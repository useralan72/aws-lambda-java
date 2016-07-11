package com.example.lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.service.RouterService;
import com.example.spring.Application;

public class LambdaFunctionHandler implements RequestHandler<Object, Object> {
	
	RouterService routerService;
	
	private HttpClient httpClient;
	
	public LambdaFunctionHandler() {
		httpClient = HttpClientBuilder.create().build();
	}

    @Override
    public Object handleRequest(Object input, Context context) {
    	//request is just a map - cast 
    	Map<Object, Object> request = (Map) input;
    	context.getLogger().log("Request: " + request.toString());
    	//load properties for routing
    	routerService = Application.getBean(RouterService.class);
    	routerService.routes().forEach((t,u) -> context.getLogger().log("Property: " + t + "\nValue: " + u + "\n"));
    	
    	//get the hostname from the routing file
    	String hostName = routerService.routes().get(request.get("utility"));
    	
    	//replace {}
        HttpGet httpGET = new HttpGet(hostName + getResourceURLFormatted(request));
        //set any headers coming in 
        httpGET.setHeaders(getHeadersFromRequest(request));
        //log what has been sent across
        context.getLogger().log("HttpGet: url " + httpGET.toString());
		for(Header h: httpGET.getAllHeaders()){
			context.getLogger().log(h.getName() + ": " + h.getValue());
		}
		
		HttpResponse response = null;

		// Execute your request and throw response
		try {
			response = httpClient.execute(httpGET);
			context.getLogger().log("Response is: " + EntityUtils.toString(response.getEntity()));
		} catch (IOException e) {
			context.getLogger().log(e.getMessage());
		} 
     
        return response;
    }
    
    /**
     * Recreate the url to use
     * @param requestMap
     * @return
     */
    public Object getResourceURLFormatted(Map<Object, Object> requestMap) {
    	//create buffer and add resource apth
    	StringWriter writer = new StringWriter();
    	writer.write(requestMap.get("resource_path").toString());
    	requestMap.forEach((k,v)->{
    		String keyWithBrackets = "{" + k.toString() + "}";
    		if(writer.toString().contains(keyWithBrackets)){
    			String resourceUrl = writer.toString();
    			writer.getBuffer().setLength(0);
    			writer.write(resourceUrl.replace(keyWithBrackets, v.toString()));
    		} 
    	});
    	return writer.toString();
    }
    
    public Header[] getHeadersFromRequest(Map<Object, Object> requestMap) {
    	List<Header> headers = new ArrayList<Header>();
    	requestMap.forEach((k,v)->{
    		if(k.toString().contains("HEADER_")){
    			headers.add(new BasicHeader(k.toString(), v.toString()));
    		} 
    	});
    	return headers.toArray(new Header[headers.size()]);
    }
    	
}
