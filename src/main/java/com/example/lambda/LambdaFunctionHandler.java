package com.example.lambda;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.lambda.model.JSONResponse;
import com.example.service.RouterService;
import com.example.spring.Application;

public class LambdaFunctionHandler implements RequestHandler<Object, Object> {
	
	RouterService routerService;
	
	public HttpClient httpClient;
	
	public LambdaFunctionHandler() {
		httpClient = HttpClientBuilder.create().build();
		//load properties for routing
		routerService = Application.getBean(RouterService.class);
	}

    @Override
    public Object handleRequest(Object input, Context context) {
    	context.getLogger().log("input object type: " + input.getClass().getName());
    	//request is just a map - cast 
    	Map<Object, Object> request = (Map) input;
    	//log loaded router properties
    	routerService.routes().forEach((t,u) -> context.getLogger().log("Property: " + t + "\nValue: " + u + "\n"));
    	
    	//get the hostname from the routing file
    	String hostName = routerService.routes().get(request.get("utility"));
    	
    	//recreate the HTTP request by replacing {} path parameters
        HttpGet httpGET = new HttpGet(hostName + getResourceURLFormatted(request));
        //set any headers coming in 
        httpGET.setHeaders(getHeadersFromRequest(request));
        //log what has been sent across
        context.getLogger().log("HttpGet: url " + httpGET.toString());
		for(Header h: httpGET.getAllHeaders()){
			context.getLogger().log(h.getName() + ": " + h.getValue());
		}
		
		HttpResponse response = null;
		JSONResponse jsonResponse = new JSONResponse();

		// Execute your request and throw response
		try {
			response = httpClient.execute(httpGET);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			     String message = Integer.toString(response.getStatusLine().getStatusCode()) + 
			    		 "[" + response.getStatusLine().getReasonPhrase() + "]";
			     throw new RuntimeException(message);
			}
			
			jsonResponse.setBody(EntityUtils.toString(response.getEntity()));
			extractResponseHeaders(response, jsonResponse);
			
		} catch (Exception e) {
			context.getLogger().log(e.getMessage());
			throw new RuntimeException(e);
		}
        return jsonResponse;
    }

	protected void extractResponseHeaders(HttpResponse response, JSONResponse jsonResponse) {
		Header[] headers = response.getAllHeaders();
		if (headers.length > 0) {
			Map<String, String> headerMap = new HashMap<String, String>(headers.length);
			for (Header header : headers) {
				headerMap.put(header.getName(), header.getValue());
			}
			jsonResponse.setHeaders(headerMap);
		}
	}
    
    /**
     * Recreate the request url to use
     * @param requestMap
     * @return
     */
	protected Object getResourceURLFormatted(Map<Object, Object> requestMap) {
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
    
    /**
     * Get the custom headers and return 
     * @param requestMap
     * @return Header[]
     */
	protected Header[] getHeadersFromRequest(Map<Object, Object> requestMap) {
    	List<Header> headers = new ArrayList<Header>();
    	requestMap.forEach((k,v)->{
    		if(k.toString().contains("NEPTUNE_HEADER_")){
    			headers.add(new BasicHeader(k.toString(), v.toString()));
    		} 
    	});
    	return headers.toArray(new Header[headers.size()]);
    }
    	
}
