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

import edu.jhu.jerboa.sim.SLSH;

public class DistributionalContextReducer extends
		Reducer<Text, MapWritable, SignatureWritable, NullWritable> {

	private int minCount;
	private SLSH slsh;

	public void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		minCount = conf.getInt("thrax.min-phrase-count", 3);
		
		try {
			slsh = new SLSH();
			slsh.initialize(conf.getInt("thrax.lsh-num-bits", 256),
					conf.getInt("thrax.lsh-pool-size", 100000),
					conf.getInt("thrax.lsh-random-seed", 42));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}		
		return;
	}

	protected void reduce(Text key, Iterable<MapWritable> values, Context context)
			throws IOException, InterruptedException {
		HashMap<String, Integer> output_map = new HashMap<String, Integer>();
		for (MapWritable input_map : values) {
			for (Writable feature_text : input_map.keySet()) {
				String feature_string = ((Text) feature_text).toString();
				int feature_value = ((IntWritable) input_map.get(feature_text)).get();
				Integer current_value = output_map.get(feature_string);
				if (current_value != null)
					output_map.put(feature_string, current_value + feature_value);
				else
					output_map.put(feature_string, feature_value);
			}
		}
		
		int count = output_map.get("count");
		if (count >= minCount) {
			for (String feature_name : output_map.keySet()) {
				if (!"count".equals(feature_name))
					slsh.update(key.toString(), feature_name,
							output_map.get(feature_name).doubleValue());
			}
			slsh.buildSignatures(true);
			context.write(new SignatureWritable(key,
					slsh.getSignature(key.toString()), count, count), NullWritable.get());
		}
		
		return;
	}
}
