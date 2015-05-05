package com.ksyun.ks3.service;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.PostObjectFormFields;
import com.ksyun.ks3.dto.ResponseHeaderOverrides;
import com.ksyun.ks3.request.WithOutContentTypeInitMultipartUploadRequest;
import com.ksyun.ks3.service.multipartpost.FormFieldKeyValuePair;
import com.ksyun.ks3.service.multipartpost.HttpPostEmulator;
import com.ksyun.ks3.service.multipartpost.UploadFileItem;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.service.response.InitiateMultipartUploadResponse;
import com.ksyun.ks3.utils.DateUtils;
import com.ksyun.ks3.utils.DateUtils.DATETIME_PROTOCOL;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月5日 上午10:58:25
 * 
 * @description 
 **/
public class ObjectMetaTest extends Ks3ClientTest{
	private String bucketName = "kss-java-sdk-test-meta";
	private String key = "test";
	Date expire = new Date();
	String cache = "no-cache";
	String disposition = "attachment; filename=fname.ext";
	String encoding = "gzip";
	String type = "text/plain-xx";
	String lang = "ZH-cn";
	@Before
	public void createBucket(){
		if(client.bucketExists(bucketName))
		{
			client.clearBucket(bucketName);
		}else{
			client.createBucket(bucketName);
		}
	}
	@After
	public void deleteBucket(){
		HeadObjectResult result = client.headObject(bucketName,key);
		assertEquals(cache,result.getObjectMetadata().getCacheControl());
		assertEquals(disposition,result.getObjectMetadata().getContentDisposition());
		assertEquals(encoding,result.getObjectMetadata().getContentEncoding());
		assertEquals(type,result.getObjectMetadata().getContentType());
		assertEquals(
				DateUtils.convertDate2Str(expire, DATETIME_PROTOCOL.RFC1123).toString(),
				DateUtils.convertDate2Str(result.getObjectMetadata().getHttpExpiresDate(), DATETIME_PROTOCOL.RFC1123).toString()
				);
		
		
		GetObjectRequest request = new GetObjectRequest(bucketName,key);
		ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
		overrides.setContentLanguage(lang);
		request.setOverrides(overrides);
		GetObjectResult res = client.getObject(request);
		assertEquals(null,res.getObject().getObjectMetadata().getCacheControl());
		assertEquals(null,res.getObject().getObjectMetadata().getContentDisposition());
		assertEquals(null,res.getObject().getObjectMetadata().getContentEncoding());
		assertEquals(type,res.getObject().getObjectMetadata().getContentType());
		assertEquals(
				DateUtils.convertDate2Str(expire, DATETIME_PROTOCOL.RFC1123).toString(),
				DateUtils.convertDate2Str(result.getObjectMetadata().getHttpExpiresDate(), DATETIME_PROTOCOL.RFC1123).toString()
				);
		assertEquals(lang,res.getObject().getObjectMetadata().getMeta("Content-Language"));
		client.clearBucket(bucketName);
	}
	@Test
	public void putObjectAndCheckMeta(){
		
		
		ObjectMetadata meta = new ObjectMetadata();
		meta.setCacheControl(cache);
		meta.setContentDisposition(disposition);
		meta.setContentEncoding(encoding);
		meta.setContentType(type);
		meta.setHttpExpiresDate(expire);
		PutObjectRequest request = new PutObjectRequest(bucketName,key,new ByteArrayInputStream("123456".getBytes()),meta);
		client.putObject(request);
	}
	@Test
	public void postObjectAndCheckMeta() throws Exception{
		Map<String,String> postData = new HashMap<String,String>();
		
		postData.put("Cache-Control",cache);
		postData.put("Content-Type", type);
		postData.put("Content-Disposition",disposition);
		postData.put("Content-Encoding",encoding);
		postData.put("Expires", DateUtils.convertDate2Str(expire, DATETIME_PROTOCOL.RFC1123).toString());
		postData.put("key",key);
		
		
		PostObjectFormFields fields = client.postObject(bucketName, key, postData, null);
		
		postData.put("policy",fields.getPolicy());
		postData.put("KSSAccessKeyId",fields.getKssAccessKeyId());
		postData.put("signature",fields.getSignature());
		
		String serverUrl = "http://"+ClientConfig.getConfig().getStr(ClientConfig.END_POINT)+"/"+bucketName;//上传地址  
          
	    // 设定要上传的普通Form Field及其对应的value  
	    ArrayList<FormFieldKeyValuePair> ffkvp = new ArrayList<FormFieldKeyValuePair>();  
	    
	    for(Entry<String,String> entry:postData.entrySet()){
	    	ffkvp.add(new FormFieldKeyValuePair(entry.getKey(),entry.getValue()));
	    }
	  
	    // 设定要上传的文件  
	    ArrayList<UploadFileItem> ufi = new ArrayList<UploadFileItem>();  
	    ufi.add(new UploadFileItem("file", System.getProperty("user.dir")+"/src/test/resources/git.exe")); 
	        
	        
	    HttpPostEmulator hpe = new HttpPostEmulator();  
	    Map<String, List<String>> response = hpe.sendHttpPostRequest(serverUrl, ffkvp, ufi,new HashMap<String,String>());  
	    System.out.println("Responsefrom server is: " + response);   
	}
	@Test
	public void mulitiUploadAndCheckMeta(){
		ObjectMetadata meta = new ObjectMetadata();
		meta.setCacheControl(cache);
		meta.setContentDisposition(disposition);
		meta.setContentEncoding(encoding);
		meta.setContentType(type);
		meta.setHttpExpiresDate(expire);
		InitiateMultipartUploadRequest request1 = new InitiateMultipartUploadRequest(bucketName,key);
		request1.setObjectMeta(meta);
		InitiateMultipartUploadResult result = client.execute(request1,InitiateMultipartUploadResponse.class);
		
		UploadPartRequest request = new UploadPartRequest(bucketName,key,result.getUploadId(),1,new ByteArrayInputStream("123456".getBytes()),6);
		client.uploadPart(request);	
		
		ListPartsResult parts = client.listParts(bucketName, key, result.getUploadId());

		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(parts);
		client.completeMultipartUpload(compRequest);
	}
}
