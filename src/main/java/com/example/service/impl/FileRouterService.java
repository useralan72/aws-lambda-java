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
	
	private String  bucketName;
	private String fileName;
	
	public FileRouterService() {
		this(BUCKETNAME, FILENAME);
	}
	
	public FileRouterService(String bucketName, String fileName) {
		this.bucketName = bucketName;
		this.fileName = fileName;
	}

	@Override
	public Map<String, String> routes() {
		Properties properties = new Properties();
    	AmazonS3 client = new AmazonS3Client();
    	S3Object xFile = client.getObject(bucketName, fileName);
    	InputStream contents = xFile.getObjectContent(); 
    	try {
    		properties.load(contents);
			contents.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        Map<String, String> mapOfProperties = properties.entrySet().stream()
        		.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
    	return mapOfProperties;
	}

}
