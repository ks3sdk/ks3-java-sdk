package com.ksyun.ks3.unit;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ksyun.ks3.dto.Bucket;
import com.ksyun.ks3.dto.BucketCorsConfiguration;
import com.ksyun.ks3.dto.BucketLoggingStatus;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.CompleteMultipartUploadResult;
import com.ksyun.ks3.dto.CorsRule;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.HeadBucketResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.Ks3Object;
import com.ksyun.ks3.dto.ListMultipartUploadsResult;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.MultiPartUploadInfo;
import com.ksyun.ks3.dto.ObjectListing;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.Part;
import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.dto.PutObjectResult;
import com.ksyun.ks3.dto.ResponseHeaderOverrides;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.dto.CorsRule.AllowedMethods;
import com.ksyun.ks3.exception.serviceside.BucketAlreadyExistsException;
import com.ksyun.ks3.exception.serviceside.BucketNotEmptyException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.HeadObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutBucketCorsRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.Base64;
import com.ksyun.ks3.utils.Converter;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;

public class Ks3ClientTest extends BaseTest{
	@Test
	public void testListBuckets(){
		List<Bucket> buckets = client.listBuckets();
		boolean found = false;
		for(Bucket b:buckets){
			if(b.getName().equals(bucket))
				found =true;
		}
		assertTrue(found);
	}
	@Test
	public void testHeadBucket(){
		HeadBucketResult ret = client.headBucket(bucket);
		assertEquals(ret.getStatueCode(),200);
	}
	@Test(expected=BucketNotEmptyException.class)
	public void testDeleteBucket(){
		client.putObject(bucket, key,"123");
		client.deleteBucket(bucket);
	}
	@Test
	public void testGetBucketAcl(){
		assertEquals(CannedAccessControlList.Private,client.getBucketCannedACL(bucket));
	}
	@Test(expected=BucketAlreadyExistsException.class)
	public void testPutBucket(){
		client.createBucket(bucket);
	}
	@Test
	public void testPutBucketAcl(){
		client.putBucketACL(bucket, CannedAccessControlList.PublicReadWrite);
		assertEquals(CannedAccessControlList.PublicReadWrite,client.getBucketCannedACL(bucket));
	}
	@Test
	public void testListObjects(){
		client.putObject(bucket, key,"123");
		ObjectListing objects = client.listObjects(bucket);
		assertEquals(1,objects.getObjectSummaries().size());
		assertEquals(key,objects.getObjectSummaries().get(0).getKey());
	}
	@Test
	public void testPutAndGetBucketLogging(){
		client.putBucketLogging(bucket, true,bucket);
		BucketLoggingStatus logging = client.getBucketLogging(bucket);
		assertEquals(true,logging.isEnable());
		assertEquals(bucket,logging.getTargetBucket());
		
		client.putBucketLogging(bucket, false, null);
		BucketLoggingStatus loggingDis = client.getBucketLogging(bucket);
		assertEquals(false,loggingDis.isEnable());
	}
	@Test
	public void testGetBucketLocation(){
		assertEquals("HANGZHOU",client.getBucketLoaction(bucket).toString());
	}
	@Test
	public void testDeleteObject(){
		client.putObject(bucket, key, "123");
		assertTrue(client.objectExists(bucket, key));
		client.deleteObject(bucket, key);
		assertFalse(client.objectExists(bucket, key));
	}
	@Test
	public void testDeleteObjects(){
		client.putObject(bucket, key, "123");
		assertTrue(client.objectExists(bucket, key));
		client.deleteObjects(new String[]{key},bucket);
		assertFalse(client.objectExists(bucket, key));
	}
	@Test
	public void testPutGetHeadStringObject() throws IOException{
		client.putObject(bucket, key, "123中文");
		GetObjectResult getObjectRet = client.getObject(bucket, key);
		Ks3Object obj = getObjectRet.getObject();
		assertEquals(bucket,obj.getBucketName());
		assertEquals(key,obj.getKey());
		assertEquals("123中文",StringUtils.inputStream2String(obj.getObjectContent()));
		
		assertEquals(9,obj.getObjectMetadata().getContentLength());
		
		HeadObjectResult headObjRet = client.headObject(bucket, key);
		assertEquals(9,headObjRet.getObjectMetadata().getContentLength());
	}
	@Test
	public void testGetObjectHeaderOverrides(){
		client.putObject(bucket, key, "123中文");
		GetObjectRequest getReq = new GetObjectRequest(bucket,key);
		ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
		overrides.setContentType("application/json");
		getReq.setOverrides(overrides);
		
		Ks3Object obj = client.getObject(getReq).getObject();
		assertEquals("application/json",obj.getObjectMetadata().getContentType());
		assertEquals("123中文",StringUtils.inputStream2String(obj.getObjectContent()));
		
	}
	@Test
	public void testPutGetHeadFileObject() throws FileNotFoundException, IOException{
		TestUtils.makeFile(dir+filename,1024*100);
		PutObjectRequest putReq = new PutObjectRequest(bucket,key,new File(dir+filename));
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType("application/pdf");
		meta.setUserMeta("example", "example");
		putReq.setObjectMeta(meta);
		client.putObject(putReq);
		assertTrue(client.objectExists(bucket, key));
		
		GetObjectResult getRet = client.getObject(bucket, key);
		assertEquals(bucket,getRet.getObject().getBucketName());
		assertEquals(key,getRet.getObject().getKey());
		assertEquals("application/pdf",getRet.getObject().getObjectMetadata().getContentType());
		assertEquals(1024*100,getRet.getObject().getObjectMetadata().getContentLength());
		assertEquals(Base64.encodeAsString(Md5Utils.computeMD5Hash(new File(dir+filename))),TestUtils.rangeGetMD5(client, bucket, key));
		assertEquals(Base64.encodeAsString(Md5Utils.computeMD5Hash(new File(dir+filename))),getRet.getObject().getObjectMetadata().getContentMD5());
		
		HeadObjectResult headRet = client.headObject(bucket, key);
		assertEquals("application/pdf",headRet.getObjectMetadata().getContentType());
		assertEquals("example",headRet.getObjectMetadata().getUserMeta("example"));
		assertEquals(1024*100,headRet.getObjectMetadata().getContentLength());
		assertEquals(Base64.encodeAsString(Md5Utils.computeMD5Hash(new File(dir+filename))),headRet.getObjectMetadata().getContentMD5());
	}
	@Test
	public void testPutObjectWithAcl(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,new ByteArrayInputStream("123".getBytes()),null);
		req.setCannedAcl(CannedAccessControlList.PublicRead);
		client.putObject(req);
		
		assertEquals(CannedAccessControlList.PublicRead,client.getObjectCannedACL(bucket, key));
	}
	@Test
	public void testObjectAcl(){
		client.putObject(bucket, key, "123");
		assertEquals(CannedAccessControlList.Private,client.getObjectCannedACL(bucket, key));
		client.putObjectACL(bucket, key, CannedAccessControlList.PublicRead);
		assertEquals(CannedAccessControlList.PublicRead,client.getObjectCannedACL(bucket, key));
	}
	@Test
	public void testCopyObject(){
		client.putObject(bucket,key,"123");
		client.copyObject(bucket, key_copy, bucket, key);
		assertTrue(client.objectExists(bucket, key_copy));
	}
	@Test
	public void testMultipartUpload() throws IOException{
		InitiateMultipartUploadRequest initReq = new InitiateMultipartUploadRequest(bucket,key);
		initReq.setCannedAcl(CannedAccessControlList.PublicRead);
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType("application/pdf");
		meta.setUserMeta("example","example");
		meta.setSseAlgorithm("AES256");
		initReq.setObjectMeta(meta);
		InitiateMultipartUploadResult initRet = client.initiateMultipartUpload(initReq);
		assertEquals(bucket,initRet.getBucket());
		assertEquals(key,initRet.getKey());
		assertNotNull(initRet.getUploadId());
		assertEquals("AES256",initRet.getSseAlgorithm());
		
		ListMultipartUploadsResult listMultiRet = client.listMultipartUploads(bucket);
		assertEquals(bucket,listMultiRet.getBucket());
		boolean found =false;
		MultiPartUploadInfo info = null;
		for(MultiPartUploadInfo ainfo : listMultiRet.getUploads()){
			info = ainfo;
			if(info.getUploadId().equals(initRet.getUploadId())){
				found = true;
				break;
			}
			else
				client.abortMultipartUpload(bucket, key, info.getUploadId());
		}
		assertTrue(found);
		assertEquals(key,info.getKey());
		assertEquals("STANDARD",info.getStorageClass());
		assertEquals(initRet.getUploadId(),info.getUploadId());
		
		UploadPartRequest upReq = new UploadPartRequest(bucket,key);
		upReq.setInputStream(new ByteArrayInputStream("123".getBytes()));
		upReq.setPartNumber(1);
		upReq.setLastPart(true);
		upReq.setPartSize(3);
		upReq.setUploadId(initRet.getUploadId());
		PartETag upRet = client.uploadPart(upReq);
		assertEquals(1,upRet.getPartNumber());
		assertEquals(Base64.encodeAsString(Md5Utils.computeMD5Hash(new ByteArrayInputStream("123".getBytes()))),Converter.ETag2MD5(upRet.geteTag()));
		assertEquals("AES256",upRet.getSseAlgorithm());
		
		ListPartsResult listRet = client.listParts(bucket, key, initRet.getUploadId());
		assertEquals(bucket,listRet.getBucketname());
		assertEquals(key,listRet.getKey());
		assertEquals(initRet.getUploadId(),listRet.getUploadId());
		assertEquals(1,listRet.getParts().size());
		Part part = listRet.getParts().get(0);
		assertEquals(1,part.getPartNumber());
		assertEquals(upRet.geteTag(),"\""+part.getETag()+"\"");
		
		CompleteMultipartUploadResult compRet = client.completeMultipartUpload(listRet);
		assertEquals(bucket,compRet.getBucket());
		assertEquals(key,compRet.getKey());
		assertEquals("AES256",compRet.getSseAlgorithm());
		
		assertTrue(client.objectExists(bucket, key));
		assertEquals(CannedAccessControlList.PublicRead,client.getObjectCannedACL(bucket, key));
		
		HeadObjectResult headRet =  client.headObject(bucket, key);
		assertEquals("application/pdf",headRet.getObjectMetadata().getContentType());
		assertEquals("example",headRet.getObjectMetadata().getUserMeta("example"));
		assertEquals("AES256",headRet.getObjectMetadata().getSseAlgorithm());
	}
	@Test
	public void testPutObjectWithSSE(){
		TestUtils.makeFile(dir+filename, 1024*100);
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		ObjectMetadata meta = new ObjectMetadata();
		meta.setSseAlgorithm("AES256");
		req.setObjectMeta(meta);
		PutObjectResult putRet = client.putObject(req);
		assertEquals("AES256",putRet.getSseAlgorithm());
		
		GetObjectResult getRet = client.getObject(bucket, key);
		assertEquals("AES256",getRet.getObject().getObjectMetadata().getSseAlgorithm());
	}
	@Test
	public void testPutObjectWithSSEC(){
		TestUtils.makeFile(dir+filename, 1024*100);
		
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		SSECustomerKey ssec = new SSECustomerKey(symKey);
		req.setSseCustomerKey(ssec);
		PutObjectResult putRet = client.putObject(req);
		assertEquals(ssec.getAlgorithm(),putRet.getSseCustomerAlgorithm());
		assertEquals(Base64.encodeAsString(Md5Utils.computeMD5Hash(Base64.decode(ssec.getBase64EncodedKey()))),putRet.getSseCustomerKeyMD5());
		
		HeadObjectRequest headReq = new HeadObjectRequest(bucket,key);
		headReq.setSseCustomerKey(ssec);
		HeadObjectResult headRet = client.headObject(headReq);
		assertEquals(ssec.getAlgorithm(),headRet.getObjectMetadata().getSseCustomerAlgorithm());
		assertEquals(Base64.encodeAsString(Md5Utils.computeMD5Hash(Base64.decode(ssec.getBase64EncodedKey()))),headRet.getObjectMetadata().getSseCustomerKeyMD5());
	}
	@Test
	public void testBucketCORS(){
		BucketCorsConfiguration config = new BucketCorsConfiguration();
		List<CorsRule> rules = new ArrayList<CorsRule>();
		
		CorsRule rule = new CorsRule();
		List<AllowedMethods> allowedMethods = new ArrayList<AllowedMethods>();
		allowedMethods.add(AllowedMethods.POST);
		List<String> allowedOrigins = new ArrayList<String>();
		allowedOrigins.add("http://*.ele.com");
		List<String> exposedHeaders = new ArrayList<String>();
		exposedHeaders.add(HttpHeaders.XKssServerSideEncryption.toString());
		List<String> allowedHeaders = new ArrayList<String>();
		allowedHeaders.add("*"); 

		rule.setAllowedHeaders(allowedHeaders);
		rule.setAllowedMethods(allowedMethods);
		rule.setAllowedOrigins(allowedOrigins);
		rule.setExposedHeaders(exposedHeaders);
		rule.setMaxAgeSeconds(11);
		rules.add(rule);
		config.setRules(rules);
		client.putBucketCors(new PutBucketCorsRequest(bucket,config));
		
		BucketCorsConfiguration getCors = client.getBucketCors(bucket);
		assertEquals(1,getCors.getRules().size());
		CorsRule getRule = getCors.getRules().get(0);
		assertEquals(1,getRule.getAllowedHeaders().size());
		assertEquals(1,getRule.getAllowedMethods().size());
		assertEquals(1,getRule.getAllowedOrigins().size());
		assertEquals(1,getRule.getExposedHeaders().size());
		
		assertEquals(HttpHeaders.XKssServerSideEncryption.toString(),getRule.getExposedHeaders().get(0));
		assertEquals("http://*.ele.com",getRule.getAllowedOrigins().get(0));
		assertEquals(AllowedMethods.POST,getRule.getAllowedMethods().get(0));
		assertEquals("*",getRule.getAllowedHeaders().get(0));
		assertEquals(11,getRule.getMaxAgeSeconds());
		
		client.deleteBucketCors(bucket);
		BucketCorsConfiguration corsAftDel = client.getBucketCors(bucket);
		assertEquals(0,corsAftDel.getRules().size());
	}
}
