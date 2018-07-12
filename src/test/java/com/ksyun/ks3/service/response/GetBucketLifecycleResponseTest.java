package com.ksyun.ks3.service.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.dto.BucketLifecycleConfiguration;

public class GetBucketLifecycleResponseTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String xml = "<LifecycleConfiguration><Rule><ID>12345</ID><Filter><Prefix>test</Prefix></Filter><Status>Enabled</Status><Expiration><Days>5</Days></Expiration></Rule><Rule><ID>123456</ID><Filter><Prefix>345</Prefix></Filter><Status>Enabled</Status><Expiration><Days>6</Days></Expiration></Rule></LifecycleConfiguration>";
		ProtocolVersion protocolVersion = new ProtocolVersion("http",1,1);
		BasicStatusLine statusLine = new BasicStatusLine(protocolVersion,200,"OK");
		BasicHttpResponse basicResponse = new BasicHttpResponse(statusLine);
		BasicHttpEntity entity = new BasicHttpEntity();
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		entity.setContent(is);
		basicResponse.setEntity(entity);
		
		GetBucketLifecycleResponse response = new GetBucketLifecycleResponse();
		response.setHttpResponse(basicResponse);
		BucketLifecycleConfiguration config =  response.handleResponse();
		Assert.assertNotNull(config);
		Assert.assertEquals(2, config.getRules().size());
	}

}
