package com.ksyun.ks3.service.response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeleteBucketLifecycleResponseTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		DeleteBucketLifecycleResponse response = new DeleteBucketLifecycleResponse();
		int[] expect = new int[2];
		expect[0]=200;
		expect[1]=204;
		Assert.assertArrayEquals(expect, response.expectedStatus());
		response.preHandle();
	}

}
