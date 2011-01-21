package edu.jhu.thrax.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.net.URI;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;

public class ConfigFileLoader
{
	protected static final String CRED_PROPS = "AwsCredentials.properties"; 
	
	public static InputStream getConfigStream(URI configURI) throws IOException
	{
		InputStream configStream = null;
	
		if (configURI.getScheme().equalsIgnoreCase("s3n") || configURI.getScheme().equalsIgnoreCase("s3")) {
		
			InputStream resStream = ConfigFileLoader.class.getResourceAsStream(CRED_PROPS);

			if (resStream == null) {
				resStream = ConfigFileLoader.class.getResourceAsStream("/" + CRED_PROPS);
			}
			
			if (resStream == null) {
				throw new IllegalArgumentException("Could not locate " + CRED_PROPS);
			}
     	
			AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(resStream));
			configStream = s3.getObject(new GetObjectRequest(configURI.getHost(), configURI.getPath().replaceFirst("/+", ""))).getObjectContent();  
		} else {
			configStream = new FileInputStream(new File(configURI));
		}
		
		return configStream;
	}
}
