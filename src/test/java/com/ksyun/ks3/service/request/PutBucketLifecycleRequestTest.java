package com.ksyun.ks3.service.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.dto.BucketLifecycleConfiguration;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Rule;
import com.ksyun.ks3.dto.BucketLifecycleConfiguration.Status;
import com.ksyun.ks3.exception.client.ClientIllegalArgumentException;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;

public class PutBucketLifecycleRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String bucketName = "test";
		Rule rule = new Rule();
		rule.setId("12345");
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(Status.ENABLED);
		

		Rule rule2 = new Rule();
		rule2.setId("123456");
		rule2.setExpirationInDays(6);
		rule2.setPrefix("345");
		rule2.setStatus(Status.ENABLED);
		rule2.setExpirationDate(new Date());
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(rule);
		rules.add(rule2);
		rule.validate();
		rule2.validate();
		
		BucketLifecycleConfiguration lifeCycle = new BucketLifecycleConfiguration(rules);
		lifeCycle.setRules(rules);
		
		PutBucketLifecycleRequest request = new PutBucketLifecycleRequest(bucketName,lifeCycle);
		Assert.assertNotNull(request.getLifecycleConfiguration());
		request.validateParams(); 
		String bucketName2 = "test2";
		Assert.assertEquals(bucketName, request.getBucketName());
		request.setBucketName(bucketName2);
		Assert.assertEquals(bucketName2, request.getBucketName());
		Request ks3Request = new Request();
		request.buildRequest(ks3Request);
		Assert.assertEquals(bucketName2,ks3Request.getBucket());
		Assert.assertEquals(HttpMethod.PUT.toString(),ks3Request.getMethod().toString());
		Assert.assertNotNull(ks3Request.getQueryParams().get("lifecycle"));
		
		
		BucketLifecycleConfiguration lifeCycleNoRules = new BucketLifecycleConfiguration(null);
		request.setLifecycleConfiguration(lifeCycleNoRules);
		Assert.assertNull(request.getLifecycleConfiguration().getRules());
		
	}
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testBucketNameException() {
		String bucketName = null;
		Rule rule = new Rule();
		rule.setId("12345");
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(Status.ENABLED);
		

		Rule rule2 = new Rule();
		rule2.setId("123456");
		rule2.setExpirationInDays(6);
		rule2.setPrefix("345");
		rule2.setStatus(Status.ENABLED);
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(rule);
		rules.add(rule2);
		rule.validate();
		rule2.validate();
		
		BucketLifecycleConfiguration lifeCycle = new BucketLifecycleConfiguration(rules);
		lifeCycle.setRules(rules);
		PutBucketLifecycleRequest request = new PutBucketLifecycleRequest(bucketName,lifeCycle);
		request.validateParams(); 
	}
	
	
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testLifecycleException() {
		String bucketName = "test";
		Rule rule = new Rule();
		rule.setId("12345");
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(Status.ENABLED);
		

		Rule rule2 = new Rule();
		rule2.setId("123456");
		rule2.setExpirationInDays(6);
		rule2.setPrefix("345");
		rule2.setStatus(Status.ENABLED);
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(rule);
		rules.add(rule2);
		rule.validate();
		rule2.validate();
		
		BucketLifecycleConfiguration lifeCycle = new BucketLifecycleConfiguration(rules);
		lifeCycle.setRules(rules);
		PutBucketLifecycleRequest request = new PutBucketLifecycleRequest(bucketName,null);
		request.validateParams(); 
	}
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testLifecycleRuleSizeException() {
		String bucketName = "test";
		Rule rule = new Rule();
		rule.setId("12345");
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(Status.ENABLED);
		

		Rule rule2 = new Rule();
		rule2.setId("123456");
		rule2.setExpirationInDays(6);
		rule2.setPrefix("345");
		rule2.setStatus(Status.ENABLED);
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(rule);
		rules.add(rule2);
		rule.validate();
		rule2.validate();
		
		for(int i = 0;i<200;i++){
			Rule rulenew = new Rule();
			rulenew.setId("123456");
			rulenew.setExpirationInDays(6);
			rulenew.setPrefix("345");
			rulenew.setStatus(Status.ENABLED);
			rules.add(rulenew);
		}
		
		BucketLifecycleConfiguration lifeCycle = new BucketLifecycleConfiguration(rules);
		lifeCycle.setRules(rules);
		PutBucketLifecycleRequest request = new PutBucketLifecycleRequest(bucketName,lifeCycle);
		request.validateParams(); 
	}
	
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testLifecycleRuleSize0Exception() {
		String bucketName = "test";
		List<Rule> rules = new ArrayList<Rule>();
		
		BucketLifecycleConfiguration lifeCycle = new BucketLifecycleConfiguration(rules);
		lifeCycle.setRules(rules);
		PutBucketLifecycleRequest request = new PutBucketLifecycleRequest(bucketName,lifeCycle);
		request.validateParams(); 
	}
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testLifecycleRuleNullException() {
		String bucketName = "test";
		
		BucketLifecycleConfiguration lifeCycle = new BucketLifecycleConfiguration(null);
		PutBucketLifecycleRequest request = new PutBucketLifecycleRequest(bucketName,lifeCycle);
		request.validateParams(); 
	}

}
