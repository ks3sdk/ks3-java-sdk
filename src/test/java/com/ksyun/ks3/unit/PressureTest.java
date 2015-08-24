package com.ksyun.ks3.unit;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.PutObjectResult;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.transfer.Ks3UploadClient;


public class PressureTest extends BaseTest {
	int maxConnections = 50;

	@Test
	public void testPutObject() {
		TestUtils.makeFile(dir + filename, 1024 * 10);
		for (int i = 0; i < 4 * maxConnections; i++) {
			client.putObject(bucket, key, new File(dir + filename));
		}
	}
	@Test
	public void testGetObject() throws Ks3ServiceException, Ks3ClientException, IOException{
		TestUtils.makeFile(dir + filename, 1024 * 10);
		client.putObject(bucket, key, new File(dir + filename));
		for (int i = 0; i < 4 * maxConnections; i++) {
			client.getObject(bucket, key).getObject().getObjectContent().close();;
		}
	}
}
