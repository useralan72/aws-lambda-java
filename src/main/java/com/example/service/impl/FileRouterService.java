package com.example.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.example.service.RouterService;

import org.springframework.stereotype.Service;

@Service
public class FileRouterService implements RouterService {
	
	private final static String BUCKETNAME = "lambda-function-bucket-us-west-2-1467892012680";
	private final static String FILENAME = "routing.properties";
	
	private String bucketName;
	private String fileName;
	private Properties properties = new Properties();
	
	public FileRouterService() throws IOException{
		this(BUCKETNAME, FILENAME);
	}
	
	public FileRouterService(String bucketName, String fileName) throws IOException{
		this.bucketName = bucketName;
		this.fileName = fileName;
    	AmazonS3 client = new AmazonS3Client();
    	S3Object xFile = client.getObject(bucketName, fileName);
    	InputStream contents = xFile.getObjectContent(); 
    	properties.load(contents);
		contents.close();	
	}

	@Override
	public Map<String, String> routes() {  
		//convert loaded properties to a map
        Map<String, String> mapOfProperties = properties.entrySet().stream()
        		.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
    	return mapOfProperties;
	}

}
