package edu.jhu.thrax.hadoop.distributional;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

public class DistributionalContextCombiner extends Reducer<Text, MapWritable, Text, MapWritable> {

	protected void reduce(Text key, Iterable<MapWritable> values, Context context)
			throws IOException, InterruptedException {
		MapWritable output_map = new MapWritable();
		for (MapWritable input_map : values) {
			for (Writable feature_name : input_map.keySet()) {
				IntWritable feature_value = (IntWritable) input_map.get(feature_name);
				if (output_map.containsKey(feature_name)) {
					IntWritable current_value = (IntWritable) output_map.get(feature_name);
					output_map.put(feature_name, new IntWritable(current_value.get() + feature_value.get()));
				} else {
					output_map.put(feature_name, new IntWritable(feature_value.get()));
				}
			}
		}
		context.write(key, output_map);
		return;
	}
}
