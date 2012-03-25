package edu.jhu.thrax.hadoop.distributional;

import java.io.IOException;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
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
		minCount = conf.getInt("thrax.min-phrase-count", 1);
		return;
	}

	protected void reduce(Text key, Iterable<MapWritable> values, Context context)
			throws IOException, InterruptedException {
		TreeMap<Text, Float> feature_map = new TreeMap<Text, Float>();
		int count = 0;
		for (MapWritable mw : values) {
			for (Writable fn : mw.keySet()) {
				Float current = feature_map.get((Text) fn);
				if (current != null)
					feature_map.put((Text) fn, (current + ((FloatWritable) mw.get(fn)).get()));
				else
					feature_map.put((Text) fn, ((FloatWritable) mw.get(fn)).get());
			}
			count++;
		}
		if (count >= minCount)
			context.write(FormatUtils.contextPhraseToText(key, feature_map),
					NullWritable.get());
		
		return;
	}
}
