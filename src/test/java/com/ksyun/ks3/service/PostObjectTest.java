package com.ksyun.ks3.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.PostObjectFormFields;
import com.ksyun.ks3.http.HttpClientFactory;
import com.ksyun.ks3.service.multipartpost.FormFieldKeyValuePair;
import com.ksyun.ks3.service.multipartpost.HttpPostEmulator;
import com.ksyun.ks3.service.multipartpost.UploadFileItem;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年1月14日 下午4:42:58
 * 
 * @description 
 **/
public class PostObjectTest extends Ks3ClientTest{
	@Test
	public void testPostObject() throws Exception{
		Map<String,String> postData = new HashMap<String,String>();
		
		postData.put("acl","public-read");
		postData.put("Cache-Control","no-cache");
		postData.put("Content-Type", "image/jpeg");
		postData.put("Content-Disposition","file;xx");
		postData.put("Content-Encoding","gzip");
		postData.put("Expires","Thu, 01 Dec 1994 16:00:00 GMT");
		postData.put("key","\\!@#$%^&*()_+-={}[]$20150115/中文\\ta$\"l\"';:><?/\\$\r;66/${filename}");
		postData.put("success_action_status","204");
		
		postData.put("x-kss-meta-xx6", "xx6");
		
		List<String> unknowValueField = new ArrayList<String>();
		unknowValueField.add("cewiuhfew");
		unknowValueField.add("fwefwefwec");
		unknowValueField.add("wqqqqqq");
		
		PostObjectFormFields fields = client.postObject("abtest", "git.exe", postData, unknowValueField);
		
		postData.put("policy",fields.getPolicy());
		postData.put("KSSAccessKeyId",fields.getKssAccessKeyId());
		postData.put("signature",fields.getSignature());
		
		String serverUrl = "http://"+ClientConfig.getConfig().getStr(ClientConfig.END_POINT)+"/abtest";//上传地址  
          
	    // 设定要上传的普通Form Field及其对应的value  
	    ArrayList<FormFieldKeyValuePair> ffkvp = new ArrayList<FormFieldKeyValuePair>();  
	    
	    for(Entry<String,String> entry:postData.entrySet()){
	    	ffkvp.add(new FormFieldKeyValuePair(entry.getKey(),entry.getValue()));
	    }
	    for(String field:unknowValueField){
	    	ffkvp.add(new FormFieldKeyValuePair(field,String.valueOf(new Random().nextDouble())));
	    }
	  
	    // 设定要上传的文件  
	    ArrayList<UploadFileItem> ufi = new ArrayList<UploadFileItem>();  
	    ufi.add(new UploadFileItem("file", System.getProperty("user.dir")+"/src/test/resources/git.exe")); 
	        
	        
	    HttpPostEmulator hpe = new HttpPostEmulator();  
	    Map<String, List<String>> response = hpe.sendHttpPostRequest(serverUrl, ffkvp, ufi,new HashMap<String,String>());  
	    System.out.println("Responsefrom server is: " + response);   
	        
	}
}
