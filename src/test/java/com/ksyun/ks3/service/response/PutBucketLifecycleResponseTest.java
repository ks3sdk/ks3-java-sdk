package com.ksyun.ks3.service.response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PutBucketLifecycleResponseTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		PutBucketLifecycleResponse response = new PutBucketLifecycleResponse();
		int[] expect = new int[1];
		expect[0]=200;
		Assert.assertArrayEquals(expect, response.expectedStatus());
		response.preHandle();
	}

}
