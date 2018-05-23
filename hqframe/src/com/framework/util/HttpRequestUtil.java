package com.framework.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
/** 
 * Http请求工具类 
 * @author 
 * @since 
 * @version 
 */
public class HttpRequestUtil {

    public static JSONObject httpRequest_json(String requestUrl,Map<String, String> params,String requestMethod, String outputStr) {  
  		JSONObject jsonObject = null;
  		try {
  			 // 构建请求参数  
  	        StringBuffer sbParams = new StringBuffer();  
  	        if (params != null && params.size() > 0) {  
  	            for (Entry<String, String> entry : params.entrySet()) {  
  	                sbParams.append(entry.getKey());  
  	                sbParams.append("=");  
  	                sbParams.append(URLEncoder.encode(entry.getValue()));  
  	                sbParams.append("&");  
  	            }  
  	        }
  	        if(sbParams.length()>0){
  	        	String p=sbParams.toString();
  	        	p=p.substring(0, p.length()-1);
  	        	requestUrl+="?"+p;
  	        }
  			HttpClient client = new HttpClient();
  			HttpMethod httpMethod = null;
  			if ("get".equalsIgnoreCase(requestMethod)) {
  				httpMethod = new GetMethod(requestUrl);
  			} else if ("post".equalsIgnoreCase(requestMethod)) {
  				httpMethod = new PostMethod(requestUrl);
  				((PostMethod)httpMethod).setRequestBody(outputStr);
  			}
  			httpMethod.setRequestHeader("Content-Type", "text/html;charset=utf-8");
  			int code = client.executeMethod(httpMethod);
  			if (code == 200) {
  				String re=httpMethod.getResponseBodyAsString();
  				jsonObject = JSONObject.fromObject(re);
  			}else{
  				System.out.println("访问出错："+code);
  			}
  			httpMethod.releaseConnection();
  		} catch (HttpException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
  		
  	    return jsonObject;
  	}  
    
    public static String httpRequest_string(String requestUrl,Map<String, String> params,String requestMethod, String outputStr) {  
  		String restr = null;
  		try {
  			 // 构建请求参数  
  	        StringBuffer sbParams = new StringBuffer();  
  	        if (params != null && params.size() > 0) {  
  	            for (Entry<String, String> entry : params.entrySet()) {  
  	                sbParams.append(entry.getKey());  
  	                sbParams.append("=");  
  	                sbParams.append(URLEncoder.encode(entry.getValue()));  
  	                sbParams.append("&");  
  	            }  
  	        }
  	        if(sbParams.length()>0){
  	        	String p=sbParams.toString();
  	        	p=p.substring(0, p.length()-1);
  	        	requestUrl+="?"+p;
  	        }
  			HttpClient client = new HttpClient();
  			HttpMethod httpMethod = null;
  			if ("get".equalsIgnoreCase(requestMethod)) {
  				httpMethod = new GetMethod(requestUrl);
  			} else if ("post".equalsIgnoreCase(requestMethod)) {
  				httpMethod = new PostMethod(requestUrl);
  				((PostMethod)httpMethod).setRequestBody(outputStr);
  			}
  			httpMethod.setRequestHeader("Content-Type", "text/html;charset=utf-8");
  			int code = client.executeMethod(httpMethod);
  			if (code == 200) {
  				restr=httpMethod.getResponseBodyAsString();
  			}else{
  				System.out.println("访问出错："+code);
  			}
  			httpMethod.releaseConnection();
  		} catch (HttpException e) {
  			e.printStackTrace();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
  	    return restr;
  	}  
    
}