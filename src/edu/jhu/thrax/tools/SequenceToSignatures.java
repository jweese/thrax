package edu.jhu.thrax.tools;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

import edu.jhu.thrax.hadoop.distributional.SignatureWritable;

public class SequenceToSignatures {
	public static void main(String[] argv) throws Exception {
		URI uri = URI.create(argv[0]);
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(uri, conf);
		Path path = new Path(argv[0]);
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);

		SignatureWritable signature = new SignatureWritable();
		while (reader.next(signature)) {
			System.err.println(signature.key.toString() + "\t" + signature.count);
		}
		
		reader.close();
	}
}
