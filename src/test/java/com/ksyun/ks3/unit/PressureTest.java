package com.ksyun.ks3.unit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.PutObjectResult;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.service.transfer.Ks3UploadClient;


public class PressureTest extends BaseTest {
	int maxConnections = ClientConfig.getConfig().getInt(
			"httpclient.maxConnections");

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
