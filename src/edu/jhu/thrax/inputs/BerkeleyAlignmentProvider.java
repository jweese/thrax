package edu.jhu.thrax.inputs;

import java.io.IOException;

import edu.jhu.thrax.datatypes.Alignment;

public class BerkeleyAlignmentProvider extends AbstractInputProvider<Alignment>
{

	public BerkeleyAlignmentProvider(String filename) throws IOException
	{
		super(filename);
	}

	public Alignment next()
	{
		String [] line = scanner.nextLine().split("\\s+");
		int [][] map = new int[line.length][2];
		for (int i = 0; i < line.length; i++) {
			String [] indices = line[i].split("-");
			map[i][0] = Integer.parseInt(indices[0]);
			map[i][1] = Integer.parseInt(indices[1]);
		}
		return new Alignment(map);
	}

}
