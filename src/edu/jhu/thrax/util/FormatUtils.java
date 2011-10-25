package edu.jhu.thrax.util;


public class FormatUtils {

	public static boolean isNonterminal(String token) {
		return (token.charAt(0) == '[') && (token.charAt(token.length() - 1) == ']');
	}
	
	public static String stripNonterminal(String nt) {
		return nt.substring(1, nt.length() - 1);
	}
	
	public static String stripIndexedNonterminal(String nt) {
		return nt.substring(1, nt.length() - 3);
	}
	
	public static int getNonterminalIndex(String nt) {
		return Integer.parseInt(nt.substring(nt.length() - 2, nt.length() - 1));
	}
	
	public static String markup(String nt) {
		return "[" + nt + "]";
	}
	
	public static String markup(String nt, int index) {
		return "[" + nt + "," + index + "]";
	}
}
