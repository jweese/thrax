package edu.jhu.thrax.syntax;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.EmptyStackException;

import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.util.ExternalizableToUtf8;
import edu.jhu.thrax.util.io.LineReader;
import edu.jhu.thrax.util.exceptions.MalformedParseException;

public class LatticeArray implements ParseLattice, Externalizable, ExternalizableToUtf8 {

    /**
     * A random number to get rid of the warning.
     */
    private static final long serialVersionUID = 8078L;
    /**
     * Note that index stores the indices of lattice node positions, i.e. the last element of 
     * index is the terminal node, pointing to lattice.size()
     */
    private ArrayList<Integer> forwardIndex;
    private ArrayList<Integer> forwardLattice;
    private ArrayList<Integer> backwardIndex;
    private ArrayList<Integer> backwardLattice;

    private boolean useBackwardLattice = true;

    private static final int MAX_CONCATENATIONS = 3;
    private static final int MAX_CCG_SPAN = 5;
    private static final int MAX_LABELS = 100;

    private String UNARY_CATEGORY_HANDLER = "all";

    public LatticeArray() {
        forwardIndex = null;
        forwardLattice = null;
        backwardIndex = null;
        backwardLattice = null;
    }

    public LatticeArray(String parsed_line) throws MalformedParseException {
        initialize();
        appendFromPennFormat(parsed_line);
    }

    public LatticeArray(String parsed_line, String unaryHandler) throws MalformedParseException
    {
        UNARY_CATEGORY_HANDLER = unaryHandler;
        initialize();
        appendFromPennFormat(parsed_line);
    }

    /**
     * Returns a collection of single-non-terminal labels that exactly 
     * cover the specified span in the lattice. 
     */
    public Collection<Integer> getConstituentLabels(int from, int to) {
        Collection<Integer> labels = new HashSet<Integer>();
        int span_length = to - from;
        for (int i=forwardIndex.get(from); i<forwardIndex.get(from+1); i+=2) {
            int current_span = forwardLattice.get(i+1);
            if (current_span == span_length)
                labels.add(forwardLattice.get(i));
            else if (current_span < span_length) 
                break;				
        }
        return labels;
    }

    public int getOneConstituent(int from, int to)
    {
        int spanLength = to - from;
        Stack<Integer> stack = new Stack<Integer>();

        for (int i = forwardIndex.get(from); i < forwardIndex.get(from+1); i+=2) {
            int currentSpan = forwardLattice.get(i+1);
            if (currentSpan == spanLength) {
                if ("top".equals(UNARY_CATEGORY_HANDLER))
                    return forwardLattice.get(i);
                stack.push(forwardLattice.get(i));
            }
            else if (currentSpan < spanLength)
                break;
        }
        if (stack.isEmpty())
            return -1;
        if ("bottom".equals(UNARY_CATEGORY_HANDLER))
            return stack.pop();
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            String w = Vocabulary.getWord(stack.pop());
            if (sb.length() != 0)
                sb.append(":");
            sb.append(w);
        }
        String label = sb.toString();
        return Vocabulary.getId(label);
    }

    public int getOneSingleConcatenation(int from, int to)
    {
        for (int midpt = from + 1; midpt < to; midpt++) {
            int x = getOneConstituent(from, midpt);
            if (x < 0)
                continue;
            int y = getOneConstituent(midpt, to);
            if (y < 0)
                continue;
            String label = Vocabulary.getWord(x) + "+" + Vocabulary.getWord(y);
            return Vocabulary.getId(label);
        }
        return -1;
    }

    public int getOneDoubleConcatenation(int from, int to)
    {
        for (int a = from + 1; a < to - 1; a++) {
            for (int b = a + 1; b < to; b++) {
                int x = getOneConstituent(from, a);
                if (x < 0)
                    continue;
                int y = getOneConstituent(a, b);
                if (y < 0)
                    continue;
                int z = getOneConstituent(b, to);
                if (z < 0)
                    continue;
                String label = Vocabulary.getWord(x) + "+" + Vocabulary.getWord(y) + "+" + Vocabulary.getWord(z);
                return Vocabulary.getId(label);
            }
        }
        return -1;
    }

    public int getOneRightSideCCG(int from, int to)
    {
        for (int end = to + 1; end <= forwardLattice.size(); end++) {
            int x = getOneConstituent(from, end);
            if (x < 0)
                continue;
            int y = getOneConstituent(to, end);
            if (y < 0)
                continue;
            String label = Vocabulary.getWord(x) + "/" + Vocabulary.getWord(y);
            return Vocabulary.getId(label);
        }
        return -1;
    }

    public int getOneLeftSideCCG(int from, int to)
    {
        for (int start = from - 1; start >= 0; start--) {
            int x = getOneConstituent(start, to);
            if (x < 0)
                continue;
            int y = getOneConstituent(start, from);
            if (y < 0)
                continue;
            String label = Vocabulary.getWord(y) + "\\" + Vocabulary.getWord(x);
            return Vocabulary.getId(label);
        }
        return -1;
    }
                    

    /**
     * Returns a collection of concatenated non-terminal labels that exactly 
     * cover the specified span in the lattice. The number of non-terminals 
     * concatenated is limited by MAX_CONCATENATIONS and the total number 
     * of labels returned is bounded by MAX_LABELS.
     */
    public Collection<Integer> getConcatenatedLabels(int from, int to) {
        Collection<Integer> labels = new HashSet<Integer>();

        int span_length = to - from;
        Stack<Integer> nt_stack = new Stack<Integer>();
        Stack<Integer> pos_stack = new Stack<Integer>();
        Stack<Integer> depth_stack = new Stack<Integer>();

        // seed stacks (reverse order to save on iterations, longer spans)
        for (int i=forwardIndex.get(from+1)-2; i>=forwardIndex.get(from); i-=2) {
            int current_span = forwardLattice.get(i+1);
            if (current_span < span_length) {
                nt_stack.push(forwardLattice.get(i));
                pos_stack.push(from + current_span);
                depth_stack.push(1);
            }
            else if (current_span >= span_length)
                break;
        }

        while (!nt_stack.isEmpty() && labels.size()<MAX_LABELS) {
            int nt = nt_stack.pop();
            int pos = pos_stack.pop();
            int depth = depth_stack.pop();

            // maximum depth reached without filling span
            if (depth == MAX_CONCATENATIONS)
                continue;

            int remaining_span = to - pos;			
            for (int i=forwardIndex.get(pos+1)-2; i>=forwardIndex.get(pos); i-=2) {
                int current_span = forwardLattice.get(i+1);
                if (current_span > remaining_span)
                    break;

                // create and look up concatenated label
                int concatenated_nt = Vocabulary.getId(Vocabulary.getWord(nt) 
                        + "+" + Vocabulary.getWord(forwardLattice.get(i)));
                if (current_span < remaining_span) {
                    nt_stack.push(concatenated_nt);
                    pos_stack.push(pos + current_span);
                    depth_stack.push(depth + 1);
                }
                else if (current_span == remaining_span) {
                    labels.add(concatenated_nt);
                }
            }
        }

        return labels;
    }

    public Collection<Integer> getCcgLabels(int from, int to) {
        Collection<Integer> labels = new HashSet<Integer>();

        int span_length = to - from;
        // TODO: range checks on the to and from

        boolean is_prefix = (forwardLattice.get(forwardIndex.get(from) + 1) > span_length);
        if (is_prefix) {
            Map<Integer, Set<Integer>> main_constituents = new HashMap<Integer, Set<Integer>>();
            // find missing to the right
            for (int i=forwardIndex.get(from); i<forwardIndex.get(from+1); i+=2) {
                int current_span = forwardLattice.get(i+1);
                if (current_span <= span_length)
                    break;
                else {
                    int end_pos = forwardLattice.get(i+1) + from;
                    Set<Integer> nts = main_constituents.get(end_pos);
                    if (nts == null)
                        main_constituents.put(end_pos, new HashSet<Integer>());
                    main_constituents.get(end_pos).add(forwardLattice.get(i));
                }
            }
            for (int i=forwardIndex.get(to); i<forwardIndex.get(to+1); i+=2) {
                Set<Integer> main_set = main_constituents.get(to + forwardLattice.get(i+1));
                if (main_set != null) {
                    for (int main : main_set)
                        labels.add(Vocabulary.getId(Vocabulary.getWord(main) 
                                    + "/" + Vocabulary.getWord(forwardLattice.get(i))));
                }
            }
        }

        if (!is_prefix) {
            if (useBackwardLattice) {
                // check if there is any possible higher-level constituent overlapping
                int to_end = (to == backwardIndex.size()-1) ? backwardLattice.size() : backwardIndex.get(to+1);
                // check longest span ending in to..
                if (backwardLattice.get(to_end - 1) <= span_length)
                    return labels;

                Map<Integer, Set<Integer>> main_constituents = new HashMap<Integer, Set<Integer>>();
                // find missing to the left
                for (int i=to_end-2; i>=backwardIndex.get(to); i-=2) {
                    int current_span = backwardLattice.get(i+1);
                    if (current_span <= span_length)
                        break;
                    else {
                        int start_pos = to - backwardLattice.get(i+1);
                        Set<Integer> nts = main_constituents.get(start_pos);
                        if (nts == null)
                            main_constituents.put(start_pos, new HashSet<Integer>());
                        main_constituents.get(start_pos).add(backwardLattice.get(i));
                    }
                }
                for (int i=backwardIndex.get(from); i<backwardIndex.get(from+1); i+=2) {
                    Set<Integer> main_set = main_constituents.get(from - backwardLattice.get(i+1));
                    if (main_set != null) {
                        for (int main : main_set)
                            labels.add(Vocabulary.getId(Vocabulary.getWord(main) 
                                        + "\\" + Vocabulary.getWord(backwardLattice.get(i))));
                    }
                }
            }
            else {
                // TODO: bothersome no-backwards-arrays method.
            }
        }

        return labels;
    }


    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub

    }


    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * Reads Penn Treebank format file 
     */
    public void readExternalUtf8(String file_name) throws IOException {
        LineReader reader = new LineReader(file_name);

        initialize();

        for (String line : reader) {
            if (line.trim().equals(""))
                continue;
            try {
                appendFromPennFormat(line);
            }
            catch (MalformedParseException e) {
                throw new IOException(line, e);
            }
        }
    }

    public void writeExternalUtf8(String file_name) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<forwardIndex.size(); i++)
                sb.append("FI[" + i + "] =\t" + forwardIndex.get(i) + "\n");
            sb.append("\n");
            for (int i=0; i<forwardLattice.size(); i+=2)
                sb.append("F[" + i + "] =\t" + Vocabulary.getWord(forwardLattice.get(i)) + " , " + forwardLattice.get(i+1) + "\n");

            if (this.useBackwardLattice) {
                sb.append("\n");
                for (int i=0; i<backwardIndex.size(); i++)
                    sb.append("BI[" + i + "] =\t" + backwardIndex.get(i) + "\n");
                sb.append("\n");
                for (int i=0; i<backwardLattice.size(); i+=2)
                    sb.append("B[" + i + "] =\t" + Vocabulary.getWord(backwardLattice.get(i)) + " , " + backwardLattice.get(i+1) + "\n");
            }
            return sb.toString();
        }

    private void initialize() {
        forwardIndex = new ArrayList<Integer>();
        forwardIndex.add(0);
        forwardLattice = new ArrayList<Integer>();
        if (this.useBackwardLattice) {
            backwardIndex = new ArrayList<Integer>();
            backwardIndex.add(0);
            backwardLattice = new ArrayList<Integer>();
        }
    }

    private void appendFromPennFormat(String line) throws MalformedParseException {
        String[] tokens = line.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").trim().split("\\s+");

        boolean next_nt = false;
        int current_id = 0;
        Stack<Integer> stack = new Stack<Integer>();

        for (String token : tokens) {
            if ("(".equals(token)) {
                next_nt = true;
                continue;
            }
            if (")".equals(token)) {
                try {
                    int closing_pos = stack.pop();
                    forwardLattice.set(closing_pos, forwardIndex.size() - forwardLattice.get(closing_pos));
                    if (this.useBackwardLattice) {
                        backwardLattice.add(forwardLattice.get(closing_pos-1));
                        backwardLattice.add(forwardLattice.get(closing_pos));
                    }
                }
                catch (EmptyStackException e) {
                    throw new MalformedParseException(line);
                }
                continue;
            }
            if (next_nt) {
                // get NT id
                // replace "," with "COMMA"
                if (",".equals(token)) {
                    token = "COMMA";
                }
                current_id = Vocabulary.getId(token);
                // add into lattice
                forwardLattice.add(current_id);
                // push NT span field onto stack (added hereafter, we're just saving the "- 1")
                stack.push(forwardLattice.size());
                // add NT span field
                forwardLattice.add(forwardIndex.size());
            }
            else {
                current_id = Vocabulary.getId(token);
                forwardIndex.add(forwardLattice.size());
                if (this.useBackwardLattice)
                    backwardIndex.add(backwardLattice.size());
            }
            next_nt = false;
        }
    }

    // little test main.
    public static void main(String[] args) {
        LatticeArray la = new LatticeArray();

        try {
            la.readExternalUtf8(args[0]);

            // System.err.println(la);
            int from = Integer.parseInt(args[1]);
            int to = Integer.parseInt(args[2]);

            if (from < to) {
                Collection<Integer> labels;
                labels = la.getConstituentLabels(from, to);
                for (int l : labels)
                    System.err.println(Vocabulary.getWord(l));

                labels = la.getConcatenatedLabels(from, to);
                for (int l : labels)
                    System.err.println(Vocabulary.getWord(l));

                labels = la.getCcgLabels(from, to);
                for (int l : labels)
                    System.err.println(Vocabulary.getWord(l));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }	
}
