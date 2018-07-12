package com.ksyun.ks3.service.request;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.exception.client.ClientIllegalArgumentException;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;


public class GetBucketLifecycleRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String bucketName = "test";
		GetBucketLifecycleRequest request = new GetBucketLifecycleRequest(bucketName);
		request.validateParams(); 
		String bucketName2 = "test2";
		Assert.assertEquals(bucketName, request.getBucketName());
		request.setBucketName(bucketName2);
		Assert.assertEquals(bucketName2, request.getBucketName());
		Request ks3Request = new Request();
		request.buildRequest(ks3Request);
		Assert.assertEquals(bucketName2,ks3Request.getBucket());
		Assert.assertEquals(HttpMethod.GET.toString(),ks3Request.getMethod().toString());
		Assert.assertNotNull(ks3Request.getQueryParams().get("lifecycle"));
	}
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testException() {
		String bucketName = null;
		GetBucketLifecycleRequest request = new GetBucketLifecycleRequest(bucketName);
		request.validateParams(); 
	}

}
