package com.ksyun.ks3.unit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.service.request.AbortMultipartUploadRequest;

public class RequestTest{
	String bucket = "bucket";
	String key = "中文";
	@Test
	public void testAbortMultipartUpload(){
		AbortMultipartUploadRequest req = new AbortMultipartUploadRequest(bucket,key,"1234");
		req.validateParams();
		
		Request request = new Request();
		req.buildRequest(request);
		assertEquals(HttpMethod.DELETE,request.getMethod());
		assertEquals(bucket,request.getBucket());
		assertEquals(key,request.getKey());
		assertEquals("1234",request.getQueryParams().get("uploadId"));
	}
}
