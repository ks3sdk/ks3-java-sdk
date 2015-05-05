package com.ksyun.ks3.service;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.ksyun.ks3.dto.CopyResult;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.request.CopyObjectRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.HeadObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;

/**
 * @description	测试Object api中 request headers 的功能与逻辑
 * @author ZHANGZHENGYONG
 * @date 2014 年 11 月 24 日	11:22	
 */
public class ObjectHeadersTest extends ObjectBeforeTest {
	
	/**
	 * <p>The implementation of the <code>GET Object</code> can use the following 
	 * 	request headers in addition to the request headers common to all operations.
	 * <pre>
	 *  Range
	 *  If-Modified-Since
	 *  If-Unmodified-Since
	 *  If-Match
	 *  If-None-Match
	 * <pre></p>
	 */
	public void getObjectHeaders(){
	}
	
	/**
	 * <code>getObjectHeaders</code>
	 * @part Range
	 */
	@Test(timeout=10000)
	public void getObjectHeaders2101(){
		GetObjectRequest request = new GetObjectRequest(bucket, "getObjectHeaders.txt");
		
		/* part range */
		//set the headers 
		request.setRange(0, 1);
		//get the result 
		GetObjectResult result = client.getObject(request);
		//assert the setting 
		assertEquals(2, result.getObject().getObjectMetadata().getContentLength());
		
	}
	
	/**
	 * <code>getObjectHeaders</code>
	 * @part Modified-Since 
	 */
	@Test(timeout=10000)
	public void getObjectHeaders2102(){
		GetObjectRequest request = new GetObjectRequest(bucket, "getObjectHeaders.txt");
		GetObjectRequest requestSec= new GetObjectRequest(bucket, "getObjectHeaders.txt");
		
		/* part Modified-Since */
		//set the headers
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date date1 = cal.getTime();
		cal.add(Calendar.YEAR, -2);
		Date date2 = cal.getTime();
		request.setModifiedSinceConstraint(date1);	//304
		requestSec.setUnmodifiedSinceConstraint(date2);	//412
		//get the result 
		GetObjectResult result = client.getObject(request);
		GetObjectResult resultSec = client.getObject(requestSec);
		//assert the setting
		assertEquals(false, result.isIfModified()); //304
		assertEquals(false, resultSec.isIfPreconditionSuccess()); //412
		
		System.out.println("date1:"+date1+"\ndate2:"+date2);
		System.out.println(result+"\n"+resultSec);
	}
	
	/**
	 * <code>getObjectHeaders</code>
	 * @part Modified-Since
	 */
	@Test(timeout=10000)
	public void getObjectHeaders2105(){
		GetObjectRequest request = new GetObjectRequest(bucket, "getObjectHeaders.txt");
		GetObjectRequest requestSec= new GetObjectRequest(bucket, "getObjectHeaders.txt");
		
		/* part Modified-Since */
		//set the headers
		Calendar cal = Calendar.getInstance();
		Date date1 = cal.getTime();
		cal.add(Calendar.YEAR, -2);
		Date date2 = cal.getTime();
		request.setModifiedSinceConstraint(date2);
		requestSec.setUnmodifiedSinceConstraint(date1);
		
		//get the result 
		GetObjectResult result = client.getObject(request);
		GetObjectResult resultSec = client.getObject(requestSec);
		//assert the setting
		assertEquals(true, result.isIfModified()); //200
		assertEquals(true, resultSec.isIfPreconditionSuccess()); //200
		
	}
	
	/**
	 * <code>getObjectHeaders</code>
	 * @part If-Match  412
	 */
	@Test(timeout=10000)
	public void getObjectHeaders2103(){
		GetObjectRequest request = new GetObjectRequest(bucket, "getObjectHeaders.txt");
		GetObjectRequest requestSec= new GetObjectRequest(bucket, "getObjectHeaders.txt");
		//set the headers
		List<String> eTags = this.getETags();
		List<String> eTagsSec = new ArrayList<String>();
		eTagsSec.add("21c49d6cf6400735433c02cb1b6c90f1");
		
		request.setMatchingETagConstraints(eTags);
		requestSec.setNonmatchingEtagConstraints(eTagsSec);
		
		//get the result 
		GetObjectResult result = client.getObject(request);
		GetObjectResult resultSec = client.getObject(requestSec);
		//assert the setting
		assertEquals(true, result.isIfPreconditionSuccess());
		assertEquals(true, resultSec.isIfPreconditionSuccess());
		assertEquals(true, result.isIfModified());
		assertEquals(true, resultSec.isIfModified());
			
//		System.out.println(result);
//		System.out.println(resultSec);
	}
	
	/**
	 * <code>getObjectHeaders</code>
	 * @part If-Match  304
	 */
	@Test()
	public void getObjectHeaders2104(){
		GetObjectRequest request = new GetObjectRequest(bucket, "getObjectHeaders.txt");
		GetObjectRequest requestSec= new GetObjectRequest(bucket, "getObjectHeaders.txt");
		//set the headers
		List<String> eTags = this.getETags();
		List<String> eTagsSec = new ArrayList<String>();
		eTagsSec.add("21c49d6cf6400735433c02cb1b6c90f1");
		
		request.setMatchingETagConstraints(eTagsSec);
		requestSec.setNonmatchingEtagConstraints(eTags);
		
		//get the result 
		GetObjectResult result = client.getObject(request);
		GetObjectResult resultSec = client.getObject(requestSec);
		//assert the setting
		assertEquals(false, result.isIfPreconditionSuccess());//412
		assertEquals(false, resultSec.isIfModified());//304
		assertEquals(true, result.isIfModified());//412
		assertEquals(true, resultSec.isIfPreconditionSuccess());//304
			
//		System.out.println(result);
//		System.out.println(resultSec);
	}
	
	
	/**
	 * <p>The implementation of the <code>DELETE Object</code> can use the request
	 *  header named <code>x-mfa</code>, but it is not implemented.
	 * </p>
	 */
	@Test(timeout=1500)
	public void deleteObjectHeaders(){
		
	}
	
	/**
	 * <p>The implementation of the <code>PUT Object</code> can use the following 
	 * 	request headers in addition to the request headers common to all operations.
	 * <pre>
	 *  Cache-Control
	 *  Content-Disposition
	 *  Content-Encoding
	 *  Content-Length
	 *  Content-MD5
	 *  Content-Type
	 *  Expect
	 *  Expires
	 *  redirect-location
	 * <pre></p>
	 */
	@Test(timeout=10000)
	public void putObjectHeaders(){
		PutObjectRequest request = new PutObjectRequest(bucket, "putObjectHeaders.txt", new File("D:/objectTest/putObjectHeaders.txt"));

		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setCacheControl("no-cache");
		objectMeta.setContentDisposition("D:/");
		objectMeta.setContentEncoding("gb2312");
		objectMeta.setContentLength(123);
		objectMeta.setContentMD5("123dbPZABzVDPALLG2yQ8w==");
		objectMeta.setContentType("txt");
		objectMeta.setHttpExpiresDate(new Date());
		
		request.setObjectMeta(objectMeta);
		request.setRedirectLocation("http://ks3.ksyun.com/");
		client.putObject(request);
		
		GetObjectResult result = client.getObject(bucket, "putObjectHeaders.txt");

		ObjectMetadata objMeta = result.getObject().getObjectMetadata();
		System.out.println("redirectLocation"+result.getObject().getRedirectLocation());
		System.out.println(objMeta);
	}
	
	
	/**
	 * <p>The implementation of the <code>PUT Object - Copy</code> can use the following 
	 * 	request headers in addition to the request headers common to all operations.
	 * <pre>
	 *  copy-source
	 *  metadata-directive
	 *  copy-source-if-match
	 *  copy-source-if-none-match
	 *  copy-source-if-unmodified-since
	 *  copy-source-if-modified-since
	 *  redirect-location
	 * <pre></p>
	 */
	@Test
	public void copyObjectHeaders(){
		try{
			client.deleteObject(bucket, "getObjectHeaders.txt");
		}catch(Exception e){}
		CopyObjectRequest request = new CopyObjectRequest(bucket, "getObjectHeaders.txt", bucket,"putObjectHeaders.txt");
		
		CopyResult result = client.copyObject(request);
		System.out.println(result);
	}
	
	/**
	 * <p>The implementation of the <code>Initiate Multipart Upload</code> can use the following 
	 * 	request headers in addition to the request headers common to all operations.
	 * <pre>
	 *  Cache-Control
	 *  Content-Disposition
	 *  Content-Encoding
	 *  Content-Type
	 *  Expires
	 *  redirect-location
	 * <pre></p>
	 */
	@Test(timeout=3000)
	public void initiateMultipartUpload(){
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucket, "uploadObjectHeaders.txt");
		
		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setCacheControl("no-cache");
		objectMeta.setContentDisposition("D:/");
		objectMeta.setContentEncoding("gb2312");
		objectMeta.setContentType("txt");
		objectMeta.setHttpExpiresDate(new Date());
		
		request.setObjectMeta(objectMeta);
		request.setRedirectLocation("http://ks3.ksyun.com/");
		client.initiateMultipartUpload(request);
		
		GetObjectResult result = client.getObject(bucket, "putObjectHeaders.txt");

		System.out.println("redirectLocation"+result.getObject().getRedirectLocation());
		System.out.println(result);
	}
	
	/**
	 * <p>The implementation of the <code>Upload Part</code> can use the following 
	 * 	request headers in addition to the request headers common to all operations.
	 * <pre>
	 *  Content-Length
	 *  Content-MD5
	 *  Expect
	 * <pre></p>
	 */
	@Test(timeout=1500)
	public void uploadPart(){
//		PutObjectRequest request = new PutObjectRequest(bucket, "getObjectHeaders.txt", new File("D:/objectTest/headObjectHeaders"));
//		
//		PutObjectResult result = client.putObject(request);
//		System.out.println(result);
	}
	

}
