package com.ksyun.ks3;

import java.io.File;

import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.Ks3Client;
import com.ksyun.ks3.service.transfer.Ks3UploadClient;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年5月14日 下午1:32:31
 * 
 * @description 
 **/
public class RegionTest{
	String bucket = "abeijing";
	@Test
	public void test(){
		 ClientConfig.getConfig().set(ClientConfig.CLIENT_URLFORMAT, "1");
		 Ks3 client = new Ks3Client("2HITWMQXL2VBB3XMAEHQ","ilZQ9p/NHAK1dOYA/dTKKeIqT/t67rO6V2PrXUNr");
		 client.setEndpoint("kss.hangzhou.ksyun.com");
		 
		 client.listObjects(bucket);
		 client.headBucket(bucket);
		 client.putBucketACL(bucket, CannedAccessControlList.Private);
		 client.getBucketLogging(bucket);
		 client.getBucketLoaction(bucket);
		 client.putBucketLogging(bucket, false, null);
		 client.getBucketACL(bucket);
		 
		 client.putObject(bucket,"key",new File("D://phpput"));
		 client.copyObject(bucket,"key1", bucket,"key");
		 client.getObjectACL(bucket,"key");
		 client.putObjectACL(bucket,"key",CannedAccessControlList.Private);
		 client.getObjectACL(bucket,"key");
		 
		 Ks3UploadClient upClient = new Ks3UploadClient(client);
		 upClient.mutipartUploadByThreads(bucket, "key", new File("D://phpput"));
		 
		 client.deleteObject(bucket, "key");
	}
}
