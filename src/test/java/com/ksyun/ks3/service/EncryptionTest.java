/*package com.ksyun.ks3.service;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.dto.CompleteMultipartUploadResult;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.exception.serviceside.InvalidArgumentException;
import com.ksyun.ks3.exception.serviceside.MissingCustomerKeyException;
import com.ksyun.ks3.exception.serviceside.Md5ErrorForCustomerKeyException;
import com.ksyun.ks3.exception.serviceside.*;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.CopyObjectRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.HeadObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.Base64;
import com.ksyun.ks3.utils.Md5Utils;

*//**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月28日 上午11:43:09
 * 
 * @description 
 **//*
public class EncryptionTest extends Ks3ClientTest{
	private SecretKey symKey;
	private SecretKey symKey1;
	private String bucket = "test-encryption-server";
	private String key = "test";
	private String destFile = "D://serverEncryption";
	final File file = new File(this.getClass().getClassLoader().getResource("git.exe").toString().substring(6));
	@Before
	public void initKey() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		symKey = this.loadSymmetricAESKey();
		KeyGenerator symKeyGenerator = KeyGenerator.getInstance("AES");
	    symKeyGenerator.init(256); 
	    symKey1 = symKeyGenerator.generateKey();
	    
	    if(client1.bucketExists(bucket)){
	    	client1.clearBucket(bucket);
	    }else{
	    	client1.createBucket(bucket);
	    }
	}
	@Test
	public void putObjectWithSSES3() throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		HeadObjectResult ret = client1.headObject(bucket, key);
		assertEquals("AES256",ret.getObjectMetadata().getSseAlgorithm());
		GetObjectResult gret = client1.getObject(bucket, key);
		assertEquals("AES256",gret.getObject().getObjectMetadata().getSseAlgorithm());
		this.rangeGetToFileWithThreads(bucket, key,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test(expected=AlgorithmInvalidForS3Exception.class)
	public void putObjectWithSSES3WithErrorAlgm(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES128");
		req.setObjectMeta(meta);
		client1.putObject(req);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSES3AndGetWithSSES3(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		GetObjectRequest getReq = new GetObjectRequest(bucket,key);
		getReq.getRequestConfig().getExtendHeaders().put(HttpHeaders.XKssServerSideEncryption.toString(), "AES256");
		client1.getObject(getReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSES3AndGetWithSSEC(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		GetObjectRequest getReq = new GetObjectRequest(bucket,key);
		getReq.setSseCustomerKey(new SSECustomerKey(this.symKey));
		client1.getObject(getReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSES3AndHeadWithSSES3(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		HeadObjectRequest getReq = new HeadObjectRequest(bucket,key);
		getReq.getRequestConfig().getExtendHeaders().put(HttpHeaders.XKssServerSideEncryption.toString(), "AES256");
		client1.headObject(getReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSES3AndHeadWithSSEC(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		HeadObjectRequest getReq = new HeadObjectRequest(bucket,key);
		getReq.setSseCustomerKey(new SSECustomerKey(this.symKey));
		client1.headObject(getReq);
	}
	@Test
	public void putObjectWithSSES3AndCopyWithNone(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,key+"_copy",bucket,key);
		client1.copyObject(copyReq);
		
		assertEquals(null,client1.headObject(bucket, key+"_copy").getObjectMetadata().getSseAlgorithm());
		assertEquals(null,client1.getObject(bucket, key+"_copy").getObject().getObjectMetadata().getSseAlgorithm());
	}
	@Test
	public void putObjectWithSSES3AndCopyWithDestSSES3(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,key+"_copy",bucket,key);
		ObjectMetadata newmeta = new ObjectMetadata();
		newmeta.setSseAlgorithm("AES256");
		copyReq.setNewObjectMetadata(newmeta);
		client1.copyObject(copyReq);
		
		assertEquals("AES256",client1.headObject(bucket, key+"_copy").getObjectMetadata().getSseAlgorithm());	
		assertEquals("AES256",client1.getObject(bucket, key+"_copy").getObject().getObjectMetadata().getSseAlgorithm());
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSES3AndCopyWithSourceSSEC(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,key+"_copy",bucket,key);
		copyReq.setSourceSSECustomerKey(new SSECustomerKey(this.symKey));
		client1.copyObject(copyReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSES3AndCopyWithDestSSECWithErrorMD5(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,key+"_copy",bucket,key);
		SSECustomerKey destKey = new SSECustomerKey(this.symKey);
		destKey.setBase64EncodedMd5("111111");
		copyReq.setSourceSSECustomerKey(destKey);
		client1.copyObject(copyReq);
	}
	@Test
	public void putObjectWithSSES3AndCopyWithDestSSEC(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,key+"_copy",bucket,key);
		SSECustomerKey destKey = new SSECustomerKey(this.symKey);
		copyReq.setDestinationSSECustomerKey(destKey);
		client1.copyObject(copyReq);
		
		HeadObjectRequest hreq = new HeadObjectRequest(bucket, key+"_copy");
		hreq.setSseCustomerKey(destKey);
		ObjectMetadata meta1 = client1.headObject(hreq).getObjectMetadata();
		GetObjectRequest greq = new GetObjectRequest(bucket,key+"_copy");
		greq.setSseCustomerKey(destKey);
		ObjectMetadata meta2 = client1.getObject(greq).getObject().getObjectMetadata();
		assertEquals(destKey.getAlgorithm(),meta1.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(destKey.getBase64EncodedKey())),meta1.getSseCustomerKeyMD5());
		assertEquals(destKey.getAlgorithm(),meta2.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(destKey.getBase64EncodedKey())),meta2.getSseCustomerKeyMD5());
	}
	
	@Test(expected=Md5ErrorForCustomerKeyException.class)
	public void putObjectWithSSECWithErrorMD5(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		key.setBase64EncodedMd5("111111");
		req.setSseCustomerKey(key);
		client1.putObject(req);
	}
	@Test(expected=AlgorithmInvalidForCustomerKeyException.class)
	public void putObjectWithSSECWithErrorAlgm(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		key.setAlgorithm("AES128");
		req.setSseCustomerKey(key);
		client1.putObject(req);
	}
	@Test(expected=MissingCustomerKeyException.class)
	public void putObjectWithSSECAndGetWithNone(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		GetObjectRequest getReq = new GetObjectRequest(bucket,this.key);
		client1.getObject(getReq);
	}
	@Test(expected=MissingCustomerKeyException.class)
	public void putObjectWithSSECAndGetWithSSES3(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		GetObjectRequest getReq = new GetObjectRequest(bucket,this.key);
		getReq.getRequestConfig().getExtendHeaders().put(HttpHeaders.XKssServerSideEncryption.toString(),"AES256");
		client1.getObject(getReq);
	}
	@Test(expected=Md5NotMatchForOldMd5Exception.class)
	public void putObjectWithSSECAndGetWithSSECErrorSecretKey(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		GetObjectRequest getReq = new GetObjectRequest(bucket,this.key);
		getReq.setSseCustomerKey(new SSECustomerKey(this.symKey1));
		client1.getObject(getReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSECAndHeadWithNone(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		HeadObjectRequest getReq = new HeadObjectRequest(bucket,this.key);
		client1.headObject(getReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSECAndHeadWithSSES3(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		HeadObjectRequest getReq = new HeadObjectRequest(bucket,this.key);
		getReq.getRequestConfig().getExtendHeaders().put(HttpHeaders.XKssServerSideEncryption.toString(),"AES256");
		client1.headObject(getReq);
	}
	@Test(expected=InvalidArgumentException.class)
	public void putObjectWithSSECAndHeadWithSSECErrorSecretKey(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		HeadObjectRequest getReq = new HeadObjectRequest(bucket,this.key);
		getReq.setSseCustomerKey(new SSECustomerKey(this.symKey1));
		client1.headObject(getReq);
	}
	@Test(expected=Md5NotMatchForOldMd5Exception.class)
	public void putObjectWithSSECAndCopyWithNone(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,this.key+"_copy",bucket,this.key);
		client1.copyObject(copyReq);
	}
	@Test(expected=Md5NotMatchForOldMd5Exception.class)
	public void putObjectWithSSECAndCopyWithSourceErrorSecretKey(){
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,this.key+"_copy",bucket,this.key);
		copyReq.setSourceSSECustomerKey(new SSECustomerKey(this.symKey1));
		client1.copyObject(copyReq);
	}
	@Test
	public void putObjectWithSSEC() throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		HeadObjectRequest hreq = new HeadObjectRequest(bucket, this.key);
		hreq.setSseCustomerKey(key);
		ObjectMetadata meta1 = client1.headObject(hreq).getObjectMetadata();
		assertEquals(key.getAlgorithm(),meta1.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(key.getBase64EncodedKey())),meta1.getSseCustomerKeyMD5());
		
		GetObjectRequest greq = new GetObjectRequest(bucket, this.key);
		greq.setSseCustomerKey(key);
		ObjectMetadata meta2 = client1.getObject(greq).getObject().getObjectMetadata();
		assertEquals(key.getAlgorithm(),meta2.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(key.getBase64EncodedKey())),meta2.getSseCustomerKeyMD5());
		
		this.rangeGetToFileWithThreads(bucket, this.key,key,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test
	public void putObjectWithSSECAndCopyWithSourceCorrectAndDestNone() throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,this.key+"_copy",bucket,this.key);
		copyReq.setSourceSSECustomerKey(new SSECustomerKey(this.symKey));
		client1.copyObject(copyReq);
		
		ObjectMetadata meta1 = client1.headObject(bucket, this.key+"_copy").getObjectMetadata();
		assertEquals(null,meta1.getSseCustomerAlgorithm());
		assertEquals(null,meta1.getSseCustomerKeyMD5());
		ObjectMetadata meta2 = client1.getObject(bucket, this.key+"_copy").getObject().getObjectMetadata();
		assertEquals(null,meta2.getSseCustomerAlgorithm());
		assertEquals(null,meta2.getSseCustomerKeyMD5());
		
		this.rangeGetToFileWithThreads(bucket, this.key+"_copy",this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test
	public void putObjectWithSSECAndCopyWithSourceCorrectAndDestSSES3() throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,this.key+"_copy",bucket,this.key);
		copyReq.setSourceSSECustomerKey(new SSECustomerKey(this.symKey));
		ObjectMetadata newMeta = new ObjectMetadata();
		newMeta.setSseAlgorithm("AES256");
		copyReq.setNewObjectMetadata(newMeta);
		client1.copyObject(copyReq);
		
		ObjectMetadata meta1 = client1.headObject(bucket, this.key+"_copy").getObjectMetadata();
		assertEquals("AES256",meta1.getSseAlgorithm());
		ObjectMetadata meta2 = client1.getObject(bucket, this.key+"_copy").getObject().getObjectMetadata();
		assertEquals("AES256",meta2.getSseAlgorithm());
		
		this.rangeGetToFileWithThreads(bucket, this.key+"_copy",this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test
	public void putObjectWithSSECAndCopyWithSourceCorrectAndDestSSEC() throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		SSECustomerKey key = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(key);
		client1.putObject(req);
		
		SSECustomerKey destsse = new SSECustomerKey(this.symKey1);
		
		CopyObjectRequest copyReq = new CopyObjectRequest(bucket,this.key+"_copy",bucket,this.key);
		copyReq.setSourceSSECustomerKey(new SSECustomerKey(this.symKey));
		copyReq.setDestinationSSECustomerKey(destsse);
		client1.copyObject(copyReq);
		
		
		HeadObjectRequest hreq = new HeadObjectRequest(bucket, this.key+"_copy");
		hreq.setSseCustomerKey(destsse);
		ObjectMetadata meta1 = client1.headObject(hreq).getObjectMetadata();
		assertEquals(destsse.getAlgorithm(),meta1.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(destsse.getBase64EncodedKey())),meta1.getSseCustomerKeyMD5());
		
		GetObjectRequest greq = new GetObjectRequest(bucket,this.key+"_copy");
		greq.setSseCustomerKey(destsse);
		ObjectMetadata meta2 = client1.getObject(greq).getObject().getObjectMetadata();
		assertEquals(destsse.getAlgorithm(),meta2.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(destsse.getBase64EncodedKey())),meta2.getSseCustomerKeyMD5());
		
		this.rangeGetToFileWithThreads(bucket, this.key+"_copy",destsse,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test(expected=Md5ErrorForCustomerKeyException.class)
	public void initMultiWithSSECAndErrorMD5(){
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		SSECustomerKey sse = new SSECustomerKey(this.symKey);
		sse.setBase64EncodedMd5("111111");
		req.setSseCustomerKey(sse);
		client1.initiateMultipartUpload(req);
	}
	@Test(expected=MissingCustomerKeyException.class)
	public void initMultipartUploadWithSSECAndUploadWithNone(){
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		SSECustomerKey sse = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(sse);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,new ByteArrayInputStream("1234".getBytes()),4);
		client1.uploadPart(upReq);
	}
	@Test(expected=MissingCustomerKeyException.class)
	public void initMultipartUploadWithSSECAndUploadWithSSES3(){
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		SSECustomerKey sse = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(sse);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,new ByteArrayInputStream("1234".getBytes()),4);
		upReq.getRequestConfig().getExtendHeaders().put(HttpHeaders.XKssServerSideEncryption.toString(), "AES256");
		client1.uploadPart(upReq);
	}
	@Test(expected=Md5NotMatchForOldMd5Exception.class)
	public void initMultipartUploadWithSSECAndUploadWithOtherSSEC(){
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		SSECustomerKey sse = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(sse);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,new ByteArrayInputStream("1234".getBytes()),4);
		SSECustomerKey ssec = new SSECustomerKey(this.symKey1);
		upReq.setSseCustomerKey(ssec);
		client1.uploadPart(upReq);
	}
	@Test
	public void initMultipartUploadWithSSECAndUploadWithSSEC(){
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		SSECustomerKey sse = new SSECustomerKey(this.symKey);
		req.setSseCustomerKey(sse);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,new ByteArrayInputStream("1234".getBytes()),4);
		SSECustomerKey ssec = new SSECustomerKey(this.symKey);
		upReq.setSseCustomerKey(ssec);
		client1.uploadPart(upReq);
	}
	@Test
	public void multipartUploadWithSSES3() throws IOException{
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		assertEquals("AES256",initRet.getSseAlgorithm());
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,file,file.length(),0);
		PartETag partetag = client1.uploadPart(upReq);
		assertEquals("AES256",partetag.getSseAlgorithm());
		
		CompleteMultipartUploadRequest comReq = new CompleteMultipartUploadRequest(client1.listParts(bucket, key, initRet.getUploadId()));
		CompleteMultipartUploadResult comret = client1.completeMultipartUpload(comReq);
		assertEquals("AES256",comret.getSseAlgorithm());
		
		GetObjectResult gret = client1.getObject(bucket, key);
		assertEquals("AES256",gret.getObject().getObjectMetadata().getSseAlgorithm());
		this.rangeGetToFileWithThreads(bucket, key,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test
	public void multipartUploadWithSSEC() throws IOException{
		SSECustomerKey sse = new SSECustomerKey(this.symKey);
		
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		req.setSseCustomerKey(sse);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		assertEquals(sse.getAlgorithm(),initRet.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(sse.getBase64EncodedKey())),initRet.getSseCustomerKeyMD5());
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,file,file.length(),0);
		upReq.setSseCustomerKey(sse);
		PartETag partetag = client1.uploadPart(upReq);
		assertEquals(sse.getAlgorithm(),partetag.getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(sse.getBase64EncodedKey())),partetag.getSseCustomerKeyMD5());
		
		CompleteMultipartUploadRequest comReq = new CompleteMultipartUploadRequest(client1.listParts(bucket, key, initRet.getUploadId()));
		CompleteMultipartUploadResult comret = client1.completeMultipartUpload(comReq);
		assertEquals(sse.getAlgorithm(),comret.getSseCustomerAlgorithm());
		
		GetObjectRequest getReq = new GetObjectRequest(bucket, key);
		getReq.setSseCustomerKey(sse);
		GetObjectResult gret = client1.getObject(getReq);
		assertEquals(sse.getAlgorithm(),gret.getObject().getObjectMetadata().getSseCustomerAlgorithm());
		assertEquals(Md5Utils.md5AsBase64(Base64.decode(sse.getBase64EncodedKey())),gret.getObject().getObjectMetadata().getSseCustomerKeyMD5());
		this.rangeGetToFileWithThreads(bucket, key,sse,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test
	public void putObjectWithSSEKMS() throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,"test",file);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("kss:kms");
		req.setObjectMeta(meta);
		client1.putObject(req);
		HeadObjectResult ret = client1.headObject(bucket, key);
		assertEquals("kss:kms",ret.getObjectMetadata().getSseAlgorithm());
		GetObjectResult gret = client1.getObject(bucket, key);
		assertEquals("kss:kms",gret.getObject().getObjectMetadata().getSseAlgorithm());
		this.rangeGetToFileWithThreads(bucket, key,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	@Test
	public void multipartWithSSEKMS() throws IOException{
		InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest(bucket,this.key);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("kss:kms");
		req.setObjectMeta(meta);
		InitiateMultipartUploadResult initRet = client1.initiateMultipartUpload(req);
		
		assertEquals("kss:kms",initRet.getSseAlgorithm());
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key,initRet.getUploadId(),1,file,file.length(),0);
		PartETag partetag = client1.uploadPart(upReq);
		assertEquals("kss:kms",partetag.getSseAlgorithm());
		
		CompleteMultipartUploadRequest comReq = new CompleteMultipartUploadRequest(client1.listParts(bucket, key, initRet.getUploadId()));
		CompleteMultipartUploadResult comret = client1.completeMultipartUpload(comReq);
		assertEquals("kss:kms",comret.getSseAlgorithm());
		
		GetObjectResult gret = client1.getObject(bucket, key);
		assertEquals("kss:kms",gret.getObject().getObjectMetadata().getSseAlgorithm());
		this.rangeGetToFileWithThreads(bucket, key,this.destFile);
		this.checkFileMd5(file, new File(this.destFile));
	}
	private void checkFileMd5(File fie,File file1) throws FileNotFoundException, IOException{
		assertEquals(Md5Utils.md5AsBase64(file),
				Md5Utils.md5AsBase64(file1));
	}
	protected void writeToFile(InputStream content,File file) throws IOException{
		OutputStream os = new FileOutputStream(file);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		try {
			while ((bytesRead = content.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				content.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	protected void rangeGetToFile(String bucket,String key,String file) throws IOException{
		long maxLength = 1024*1024*5;
		long minLength = 1;
		long minPart = 4;
		long length = client1.headObject(bucket, key).getObjectMetadata().getContentLength();
		if(length < maxLength*minPart)
			maxLength = length / minPart;
		if(maxLength < minLength)
			maxLength = minLength+1;
		List<String> keys = new ArrayList<String>();
		for(long current = 0l;current <= length;){
			long block = (long)(Math.random()*(minLength-maxLength+1))+maxLength;
			GetObjectResult result = rangeGet(bucket,key,current,current+block);
			String filename = file+"_"+current+"-"+(current+block);
			writeToFile(result.getObject().getObjectContent(),new File(filename));
			keys.add(filename);
			current+=(block+1);
		}
		mergeFiles(file,keys.toArray());
		
	}
	protected void rangeGetToFileWithThreads(final String bucket,final String key,final SSECustomerKey sseck,final String file){
		long maxLength = 1024*1024*5;
		long minLength = 1;
		int minPart = 4;
		HeadObjectRequest headReq = new HeadObjectRequest(bucket,key);
		headReq.setSseCustomerKey(sseck);
		long length = client1.headObject(headReq).getObjectMetadata().getContentLength();
		if(length < maxLength*minPart)
			maxLength = length / minPart;
		if(maxLength < minLength)
			maxLength = minLength+1;
		
		final ExecutorService pool = Executors.newFixedThreadPool(minPart);
		final List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
		final List<String> keys = new ArrayList<String>();
		for(long current = 0l,i =0;current <= length;i++){
			long block = (long)(Math.random()*(minLength-maxLength+1))+maxLength;
			
			final long cuIndex = current;
			final long curBlock = block;
			final int curPartNum = (int)i;
			Thread t = new Thread() {
				@Override
				public void run() {
					try{
						GetObjectResult result = rangeGet(bucket,key,sseck,cuIndex,cuIndex+curBlock-1);
						String filename = file+"_"+cuIndex+"-"+(cuIndex+curBlock-1);
						try {
							writeToFile(result.getObject().getObjectContent(),new File(filename));
						} catch (IOException e) {
							e.printStackTrace();
						}
						int index = curPartNum - keys.size();
						for(;index>=0;index--)
							keys.add(null);
						keys.set((int)curPartNum, filename);
					}
					catch(RuntimeException e){
						exceptions.add(e);
					}
				}
			};
			if(exceptions.size()>0){
				pool.shutdownNow();
				break;
			}
			pool.execute(t);
			current+=block;
		}
		pool.shutdown();
		for (;;) {
			if (pool.isTerminated())
				break;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mergeFiles(file,keys.toArray());
		if(exceptions.size()>0)
			throw exceptions.get(0);	
	}
	protected void rangeGetToFileWithThreads(final String bucket,final String key,final String file) throws IOException{
		this.rangeGetToFileWithThreads(bucket, key,null, file);
	}
	private GetObjectResult rangeGet(String bucket,String key,long begin,long end){
		return rangeGet(bucket,key,null,begin,end);
	}
	private GetObjectResult rangeGet(String bucket,String key,SSECustomerKey sseck,long begin,long end){
		GetObjectRequest request = new GetObjectRequest(bucket,key);
		request.setRange(begin,end);
		request.setSseCustomerKey(sseck);
		return client1.getObject(request);
	}
	protected void mergeFiles(String outFile, Object[] files) {
		new File(outFile).delete();
		FileChannel outChannel = null;
		try {
			outChannel = new FileOutputStream(outFile).getChannel();
			for (Object f : files) {
				FileChannel fc = new FileInputStream(f.toString()).getChannel();
				ByteBuffer bb = ByteBuffer.allocate(8192);
				while (fc.read(bb) != -1) {
					bb.flip();
					outChannel.write(bb);
					bb.clear();
				}
				fc.close();
				new File(f.toString()).delete();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (outChannel != null) {
					outChannel.close();
				}
			} catch (IOException ignore) {
			}
		}
	}
	public SecretKey loadSymmetricAESKey()
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException {
		// Read private key from file.
		File keyFile = new File(this.getClass().getClassLoader().getResource("secret.key").toString().substring(6));
		FileInputStream keyfis = new FileInputStream(keyFile);
		byte[] encodedPrivateKey = new byte[(int) keyFile.length()];
		keyfis.read(encodedPrivateKey);
		keyfis.close();

		// Generate secret key.
		return new SecretKeySpec(encodedPrivateKey, "AES");
	}
}*/