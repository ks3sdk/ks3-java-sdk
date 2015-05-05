package com.ksyun.ks3.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import com.ksyun.ks3.dto.BucketCorsConfiguration;
import com.ksyun.ks3.dto.CorsRule;
import com.ksyun.ks3.dto.CorsRule.AllowedMethods;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.request.PutBucketCorsRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月5日 下午6:24:07
 * 
 * @description
 **/
public class BucketCorsTest extends Ks3ClientTest {
	final String bucketName = "testbucketcors-kssjavasdk";

	@Before
	public void createNewBucket() {
		if (!client.bucketExists(bucketName)) {
			client.createBucket(bucketName);
		}
		client.deleteBucketCors(bucketName);
	}

	@After
	public void deleteCors() {
		client.deleteBucketCors(bucketName);
	}

	/**
	 * 单次请求中包含相同的cors
	 */
	@Test
	public void testPutBucketCors() {
		BucketCorsConfiguration config = new BucketCorsConfiguration();
		for (int i = 0; i < 5; i++) {
			CorsRule rule1 = new CorsRule();
			List<AllowedMethods> allowedMethods = new ArrayList<AllowedMethods>();
			allowedMethods.add(AllowedMethods.POST);
			List<String> allowedOrigins = new ArrayList<String>();
			if(i==1){
				allowedOrigins.add("http://*.ele.com");
				allowedOrigins.add("http://*.ele.com1");
				allowedOrigins.add("http://*.ele.com3");
			}else if(i==2){
				allowedOrigins.add("http://*.ele.com3");
				allowedOrigins.add("http://*.ele.com");
				allowedOrigins.add("http://*.ele.com1");
			}else if(i==3){
				allowedOrigins.add("http://*.ele.com1");
				allowedOrigins.add("http://*.ele.com3");
				allowedOrigins.add("http://*.ele.com");
			}else{
				allowedOrigins.add("http://*.ele.com3");
				allowedOrigins.add("http://*.ele.com");
				allowedOrigins.add("http://*.ele.com1");
			}
			List<String> exposedHeaders = new ArrayList<String>();
			exposedHeaders.add(HttpHeaders.XKssServerSideEncryption.toString());
			List<String> allowedHeaders = new ArrayList<String>();
			allowedHeaders.add("*");

			rule1.setAllowedHeaders(allowedHeaders);
			rule1.setAllowedMethods(allowedMethods);
			rule1.setAllowedOrigins(allowedOrigins);
			rule1.setExposedHeaders(exposedHeaders);
			rule1.setMaxAgeSeconds(200+i);

			config.addRule(rule1);
		}

		CorsRule rule2 = new CorsRule();
		List<AllowedMethods> allowedMethods2 = new ArrayList<AllowedMethods>();
		allowedMethods2.add(AllowedMethods.GET);
		allowedMethods2.add(AllowedMethods.POST);
		List<String> allowedOrigins2 = new ArrayList<String>();
		allowedOrigins2.add("http://example.com");
		allowedOrigins2.add("http://*.example.com");
		List<String> exposedHeaders2 = new ArrayList<String>();
		exposedHeaders2.add("x-kss-test1");
		exposedHeaders2.add("x-kss-test2");
		List<String> allowedHeaders2 = new ArrayList<String>();
		allowedHeaders2.add("x-kss-test");
		allowedHeaders2.add("x-kss-test2");

		rule2.setAllowedHeaders(allowedHeaders2);
		rule2.setAllowedMethods(allowedMethods2);
		rule2.setAllowedOrigins(allowedOrigins2);
		rule2.setExposedHeaders(exposedHeaders2);
		rule2.setMaxAgeSeconds(500);

		config.addRule(rule2);

		PutBucketCorsRequest request = new PutBucketCorsRequest(bucketName,
				config);
		client.putBucketCors(request);

		BucketCorsConfiguration configResult = client.getBucketCors(bucketName);
		System.out.println(configResult);
		assertEquals(2,configResult.getRules().size());
	}
	/**
	 * 两次请求中含有相同的
	 */
	@Test
	public void testPutBucketCors1() {
		BucketCorsConfiguration config = new BucketCorsConfiguration();
		CorsRule rule1 = new CorsRule();
		List<AllowedMethods> allowedMethods = new ArrayList<AllowedMethods>();
		allowedMethods.add(AllowedMethods.POST);
		List<String> allowedOrigins = new ArrayList<String>();
		allowedOrigins.add("http://*.ele.com");
		allowedOrigins.add("http://*.ele.com1");
		allowedOrigins.add("http://*.ele.com3");
		List<String> exposedHeaders = new ArrayList<String>();
		exposedHeaders.add(HttpHeaders.XKssServerSideEncryption.toString());
		List<String> allowedHeaders = new ArrayList<String>();
		allowedHeaders.add("*");

		rule1.setAllowedHeaders(allowedHeaders);
		rule1.setAllowedMethods(allowedMethods);
		rule1.setAllowedOrigins(allowedOrigins);
		rule1.setExposedHeaders(exposedHeaders);
		rule1.setMaxAgeSeconds(200);

		config.addRule(rule1);

		CorsRule rule2 = new CorsRule();
		List<AllowedMethods> allowedMethods2 = new ArrayList<AllowedMethods>();
		allowedMethods2.add(AllowedMethods.GET);
		allowedMethods2.add(AllowedMethods.POST);
		List<String> allowedOrigins2 = new ArrayList<String>();
		allowedOrigins2.add("http://example.com");
		allowedOrigins2.add("http://*.example.com");
		List<String> exposedHeaders2 = new ArrayList<String>();
		exposedHeaders2.add("x-kss-test1");
		exposedHeaders2.add("x-kss-test2");
		List<String> allowedHeaders2 = new ArrayList<String>();
		allowedHeaders2.add("x-kss-test");
		allowedHeaders2.add("x-kss-test2");

		rule2.setAllowedHeaders(allowedHeaders2);
		rule2.setAllowedMethods(allowedMethods2);
		rule2.setAllowedOrigins(allowedOrigins2);
		rule2.setExposedHeaders(exposedHeaders2);
		rule2.setMaxAgeSeconds(500);

		config.addRule(rule2);

		PutBucketCorsRequest request = new PutBucketCorsRequest(bucketName,
				config);
		client.putBucketCors(request);

		BucketCorsConfiguration configResult = client.getBucketCors(bucketName);
		System.out.println(configResult);
		testPutBucketCors();
		assertEquals(2,configResult.getRules().size());
	}
}
