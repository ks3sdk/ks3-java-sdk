package com.ksyun.ks3.service.encryption.server;

import java.io.File;

import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.CompleteMultipartUploadResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.service.request.CopyObjectRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.HeadObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 下午5:06:36
 * 
 * @description 
 **/
public class PutObjectTest extends AWSEncryptionTest{
	@Test
	public void test(){
		PutObjectRequest request = new PutObjectRequest(bucket,"test",new File("D://test.txt"));
		
		//request.setSseCustomerKey(new SSECustomerKey(super.symKey));
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("aws:kms");
		request.setObjectMeta(meta);
		
		System.out.println(client.putObject(request));
	}
	@Test
	public void testMulti(){
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucket,"test");
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		request.setObjectMeta(meta);
		//request.setSseCustomerKey(new SSECustomerKey(super.symKey));
		InitiateMultipartUploadResult initResult = client.initiateMultipartUpload(request);
		
		UploadPartRequest upRequest = new UploadPartRequest(initResult.getBucket(),initResult.getKey(),initResult.getUploadId(),1,new File("D://test.txt"),4,0);

		//upRequest.setSseCustomerKey(new SSECustomerKey(super.symKey));
		PartETag upResult = client.uploadPart(upRequest);
		
		CompleteMultipartUploadResult coResult = client.completeMultipartUpload(client.listParts(initResult.getBucket(),initResult.getKey(),initResult.getUploadId()));
		System.out.println(initResult);
		System.out.println(upResult);
		System.out.println(coResult);
		
	}
	@Test
	public void testCopy(){
		CopyObjectRequest request = new CopyObjectRequest(bucket,"test1",bucket,"test");
		byte[]  bytes = super.symKey.getEncoded();
		
		request.setSourceSSECustomerKey(new SSECustomerKey(bytes));
		client.copyObject(request);
	}
	@Test
	public void testGet(){
		GetObjectRequest request = new GetObjectRequest(bucket,"test");
		byte[]  bytes = super.symKey.getEncoded();
		
		request.setSseCustomerKey(new SSECustomerKey(bytes));
		client.getObject(request);
	}
	@Test
	public void testHead(){
		HeadObjectRequest request = new HeadObjectRequest(bucket,"test");
		byte[]  bytes = super.symKey.getEncoded();
		
		request.setSseCustomerKey(new SSECustomerKey(bytes));
		client.headObject(request);
	}
	@Test
	public void testUserMeta(){
		ClientConfig.getConfig().set(ClientConfig.HTTP_PROTOCOL,"http");
		PutObjectRequest req = new PutObjectRequest(bucket,"test",new File("D://test.txt"));
		ObjectMetadata meta = new ObjectMetadata();
		meta.setUserMeta("test", "example");
		req.setObjectMeta(meta);
		client.putObject(req);
		
		HeadObjectResult result1 = client.headObject(bucket,"test");
		
		PutObjectRequest req1 = new PutObjectRequest(bucket,"test",new File("D://test.txt"));
		client.putObject(req1);
		
		HeadObjectResult result2 = client.headObject(bucket,"test");
		
		System.out.println(result1);
		System.out.println(result2);
	}
	@Test
	public void testCopy1(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",new File("D://test.txt"));
		req.setSseCustomerKey(new SSECustomerKey(this.symKey));
		client.putObject(req);
		
		System.out.println(client.headObject(bucket, "test"));
	}
}
