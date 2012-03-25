package edu.jhu.thrax.hadoop.distributional;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.util.FormatUtils;

public class DistributionalContextReducer extends
		Reducer<Text, MapWritable, Text, NullWritable> {

	private int minCount;

	public void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		minCount = conf.getInt("thrax.min-phrase-count", 3);
		return;
	}

	protected void reduce(Text key, Iterable<MapWritable> values, Context context)
			throws IOException, InterruptedException {
		HashMap<Text, Integer> feature_map = new HashMap<Text, Integer>();
		int count = 0;
		for (MapWritable mw : values) {
			for (Writable fn : mw.keySet()) {
				Integer current = feature_map.get((Text) fn);
				if (current != null)
					feature_map.put((Text) fn, (current + ((IntWritable) mw.get(fn)).get()));
				else
					feature_map.put((Text) fn, ((IntWritable) mw.get(fn)).get());
			}
			count++;
		}
		if (count >= minCount)
			context.write(FormatUtils.contextPhraseToText(key, feature_map),
					NullWritable.get());
		
		return;
	}
}
