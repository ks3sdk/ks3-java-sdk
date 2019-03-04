/**
 * 
 */
package com.ksyun.ks3.service.encryption.model;

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


/**
 * @author GUOZHANZHEN
 *
 */
public class BucketLifecycleConfigurationTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConfig() {
		
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
		
		Assert.assertNotNull(lifeCycle.toString());
		Assert.assertEquals(2, lifeCycle.getRules().size());
		Assert.assertEquals(6, lifeCycle.getRules().get(1).getExpirationInDays().intValue());
		Assert.assertEquals("123456", lifeCycle.getRules().get(1).getId());
		Assert.assertEquals("345", lifeCycle.getRules().get(1).getPrefix());
		Assert.assertEquals(Status.ENABLED.status2Str(), lifeCycle.getRules().get(1).getStatus().status2Str());
	}
	
	@Test
	public void testRule() {
		Rule rule = new Rule();
		rule.setId("12345");
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(Status.str2Status("Disabled"));
		
		Date date = new Date();
		
		rule.setExpirationDate(date);
		Assert.assertNotNull(rule.toString());
		Assert.assertEquals(5, rule.getExpirationInDays().intValue());
		Assert.assertEquals("12345", rule.getId());
		Assert.assertEquals("test", rule.getPrefix());
		Assert.assertEquals(Status.DISABLED.status2Str(), rule.getStatus().status2Str());
		Assert.assertEquals(date.getTime(), rule.getExpirationDate().getTime());

	}
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testRuleException() {
		Rule rule = new Rule();
		rule.setId(null);
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(Status.str2Status("Disabled"));
		
		Date date = new Date();
		
		rule.setExpirationDate(date);
		rule.validate();

	}
	
	public void testPrefixException() {
		Rule rule = new Rule();
		rule.setId("1234");
		rule.setExpirationInDays(5);
		rule.setPrefix(null);
		rule.setStatus(Status.str2Status("Disabled"));
		
		Date date = new Date();
		
		rule.setExpirationDate(date);
		rule.validate();

	}
	
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testStatusException() {
		Rule rule = new Rule();
		rule.setId("1234");
		rule.setExpirationInDays(5);
		rule.setPrefix("test");
		rule.setStatus(null);
		
		Date date = new Date();
		
		rule.setExpirationDate(date);
		rule.validate();

	}
	
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testExpireException() {
		Rule rule = new Rule();
		rule.setId("1234");
		rule.setPrefix("");
		rule.setStatus(Status.str2Status("Disabled"));
		
		
		rule.setExpirationDate(null);
		rule.validate();

	}
	
	@Test(expected = ClientIllegalArgumentException.class)
	public void testIdException() {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0;i<500;i++){
			buffer.append("test");
		}
		Rule rule = new Rule();
		rule.setId(buffer.toString());
		rule.setPrefix("1234");
		rule.setStatus(Status.str2Status("Disabled"));
		
		rule.setExpirationInDays(5);
		rule.setExpirationDate(null);
		rule.validate();

	}

}
