package edu.jhu.thrax.tools;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

import edu.jhu.thrax.hadoop.distributional.SignatureWritable;

public class SequenceToSignatures {
	public static void main(String[] argv) throws Exception {
		Configuration config = new Configuration();
		Path path = new Path(argv[0]);
		SequenceFile.Reader reader = new SequenceFile.Reader(FileSystem.getLocal(config), path, config);

		SignatureWritable signature = new SignatureWritable();
		while (reader.next(signature)) {
			System.err.println(signature.key.toString() + "\t" + signature.count);
		}
		
		reader.close();
	}
}
