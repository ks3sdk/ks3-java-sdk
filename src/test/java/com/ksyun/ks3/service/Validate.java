package com.ksyun.ks3.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.AccessControlPolicy;
import com.ksyun.ks3.dto.Bucket;
import com.ksyun.ks3.dto.BucketCorsConfiguration;
import com.ksyun.ks3.dto.BucketLoggingStatus;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.CorsRule;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.dto.CreateBucketConfiguration.REGION;
import com.ksyun.ks3.dto.Grant;
import com.ksyun.ks3.dto.GranteeUri;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.Permission;
import com.ksyun.ks3.dto.CorsRule.AllowedMethods;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.ListPartsRequest;
import com.ksyun.ks3.service.request.PutBucketCorsRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.Md5Utils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年1月6日 下午2:28:43
 * 
 * @description 对sdk的粗略验证
 **/
public class Validate extends Ks3ClientTest{
	final String bucketName = "validate-test-2015";
	final File file = new File(this.getClass().getClassLoader().getResource("git.exe").toString().substring(6));
	final String objectkey="测试中文/git.exe";
	@Before
	public void createNewBucket(){
		if(client.bucketExists(bucketName)){
			client.clearBucket(bucketName);
		}else{
			client.createBucket(bucketName);
		}
	}
	/**
	 * 列出的bucket中存在新建的
	 * @throws Exception
	 */
	@Test
	public void getService() throws Exception{
		List<Bucket> buckets = client.listBuckets();
		boolean found = false;
		for(Bucket bucket : buckets){
			if(bucketName.equals(bucket.getName())){
				found=true;
				break;
			}
		}
		if(!found)
			throw new Exception("not found");
	}
	@Test
	public void putBucketAclAndGetBucketAcl() throws Exception{
		client.putBucketACL(bucketName, CannedAccessControlList.PublicReadWrite);
		AccessControlPolicy  acl = client.getBucketACL(bucketName);
		final Collection<Permission> allUsersPermissions = new LinkedHashSet<Permission>();
		for (final Grant grant : acl.getGrants()) {
			if (GranteeUri.AllUsers.equals(grant.getGrantee())) {
				allUsersPermissions.add(grant.getPermission());
			}
		}
		final boolean read = allUsersPermissions.contains(Permission.Read);
		final boolean write = allUsersPermissions.contains(Permission.Write);
		if (read && write) {
		} else {
			throw new Exception("acl exception");
		}
	}
	@Test
	public void putBucketCorsAndGetBucketCorsAndDeleteBucketCors() throws Exception{
		BucketCorsConfiguration config = new BucketCorsConfiguration();
		CorsRule rule1 = new CorsRule();
		List<AllowedMethods> allowedMethods = new ArrayList<AllowedMethods>();
		allowedMethods.add(AllowedMethods.POST);
		List<String> allowedOrigins = new ArrayList<String>();
		allowedOrigins.add("http://*.ele.com");
		List<String> exposedHeaders = new ArrayList<String>();
		exposedHeaders.add(HttpHeaders.XKssServerSideEncryption.toString());
		List<String> allowedHeaders = new ArrayList<String>();
		allowedHeaders.add("*"); 

		rule1.setAllowedHeaders(allowedHeaders);
		rule1.setAllowedMethods(allowedMethods);
		rule1.setAllowedOrigins(allowedOrigins);
		rule1.setExposedHeaders(exposedHeaders);
		rule1.setMaxAgeSeconds(200);
		
		config.addRule(rule1);
		
		CorsRule rule2 = new CorsRule();
		List<AllowedMethods> allowedMethods2 = new ArrayList<AllowedMethods>();
		allowedMethods2.add(AllowedMethods.GET);
		allowedMethods2.add(AllowedMethods.POST);
		List<String> allowedOrigins2 = new ArrayList<String>();
		allowedOrigins2.add("http://example.com");
		allowedOrigins2.add("http://*.example.com");
		List<String> exposedHeaders2 = new ArrayList<String>();
		exposedHeaders2.add("x-kss-test1");
		exposedHeaders2.add("x-kss-test2");
		List<String> allowedHeaders2 = new ArrayList<String>();
		allowedHeaders2.add("x-kss-test"); 
		allowedHeaders2.add("x-kss-test2"); 

		rule2.setAllowedHeaders(allowedHeaders2);
		rule2.setAllowedMethods(allowedMethods2);
		rule2.setAllowedOrigins(allowedOrigins2);
		rule2.setExposedHeaders(exposedHeaders2);
		rule2.setMaxAgeSeconds(500);
		
		config.addRule(rule2);
		
		PutBucketCorsRequest request = new PutBucketCorsRequest(bucketName,config);
		client.putBucketCors(request);
		
		BucketCorsConfiguration  configResult = client.getBucketCors(bucketName);
		if(configResult.getRules().size()!=2)
			throw new Exception("get bucket cors exception");
		
		client.deleteBucketCors(bucketName);
		
		BucketCorsConfiguration  configResultDelte = client.getBucketCors(bucketName);
		if(configResultDelte.getRules().size()!=0)
			throw new Exception("delete bucket cors exception");
	}
	@Test
	public void putBucketLoggingAndGetBucketLogging() throws Exception{
		client.putBucketLogging(bucketName, true, bucketName,"prefix");
		BucketLoggingStatus status = client.getBucketLogging(bucketName);
		assertEquals(bucketName,status.getTargetBucket());
		assertEquals("prefix",status.getTargetPrefix());
	}
	@Test
	public void getBucketLocation() throws Exception{
		assertEquals(REGION.HANGZHOU,client.getBucketLoaction(bucketName));
	}
	@Test
	public void putObjectAndGetObjectAndDeleteObject() throws Ks3ServiceException, Ks3ClientException, IOException{
		client.putObject(bucketName, objectkey, file);
		GetObjectResult result = client.getObject(bucketName, objectkey);
		InputStream content = result.getObject().getObjectContent();
		
		OutputStream os = new FileOutputStream(new File("D://"
				+ "getObjectTest.txt"));
		
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
	    try {  
	        while((bytesRead = content
					.read(buffer, 0, 8192)) != -1){  
	        	os.write(buffer, 0, bytesRead);  
	        }  
	    } catch (IOException e1) {  
	        e1.printStackTrace();  
	    } finally{  
	    	os.close();
	    	content.close(); 
	    }
	    File fileDown = new File("D://getObjectTest.txt");
	    assertEquals(Md5Utils.md5AsBase64(fileDown),result.getObject().getObjectMetadata().getContentMD5());
	    fileDown.delete();
		client.deleteObject(bucketName, objectkey);
		
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(file.length());
		client.putObject(bucketName, objectkey, new FileInputStream(file), meta);
		client.deleteObject(bucketName,objectkey);
	}
	@Test
	public void putObjectAclAndGetObjectAcl() throws Exception{
		System.out.println(client.putObject(bucketName, objectkey, file));
		
		client.putObjectACL(bucketName, objectkey, CannedAccessControlList.PublicReadWrite);
		
		
		AccessControlPolicy  acl = client.getObjectACL(bucketName, objectkey);
		final Collection<Permission> allUsersPermissions = new LinkedHashSet<Permission>();
		for (final Grant grant : acl.getGrants()) {
			if (GranteeUri.AllUsers.equals(grant.getGrantee())) {
				allUsersPermissions.add(grant.getPermission());
			}
		}
		final boolean read = allUsersPermissions.contains(Permission.Read);
		final boolean write = allUsersPermissions.contains(Permission.Write);
		if (read && write) {
		} else {
			throw new Exception("acl exception");
		}
	}
	@Test
	public void multiPartUpload() {
		long part = 10 * 1024 * 1024;
		String bucket = bucketName;
		String key = objectkey;

		InitiateMultipartUploadRequest request1 = new InitiateMultipartUploadRequest(
				bucket, key);
		request1.setCannedAcl(CannedAccessControlList.PublicRead);
		InitiateMultipartUploadResult result = client
				.initiateMultipartUpload(request1);
		long n = file.length() / part;
		System.out.println(n);
		for (int i = 0; i <= n; i++) {
			UploadPartRequest request = new UploadPartRequest(
					result.getBucket(), result.getKey(), result.getUploadId(),
					i + 1, file, part, (long) i * part);
			PartETag tag = client.uploadPart(request);
			System.out.println(String.valueOf(i + 1) + "  " + tag + "\n");
		}
		// list parts
		ListPartsRequest requestList = new ListPartsRequest(result.getBucket(),
				result.getKey(), result.getUploadId());
		ListPartsResult tags = client.listParts(requestList);
		// complete
		CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
				tags);
		client.completeMultipartUpload(request);
		
		client.headObject(bucketName,objectkey);
		client.deleteObject(bucketName, objectkey);
	}
	@Test
	public void copy(){
		long part = 10 * 1024 * 1024;
		String bucket = bucketName;
		String key = objectkey;

		InitiateMultipartUploadRequest request1 = new InitiateMultipartUploadRequest(
				bucket, key);
		request1.setCannedAcl(CannedAccessControlList.PublicRead);
		InitiateMultipartUploadResult result = client
				.initiateMultipartUpload(request1);
		long n = file.length() / part;
		System.out.println(n);
		for (int i = 0; i <= n; i++) {
			UploadPartRequest request = new UploadPartRequest(
					result.getBucket(), result.getKey(), result.getUploadId(),
					i + 1, file, part, (long) i * part);
			PartETag tag = client.uploadPart(request);
			System.out.println(String.valueOf(i + 1) + "  " + tag + "\n");
		}
		// list parts
		ListPartsRequest requestList = new ListPartsRequest(result.getBucket(),
				result.getKey(), result.getUploadId());
		ListPartsResult tags = client.listParts(requestList);
		// complete
		CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
				tags);
		client.completeMultipartUpload(request);
		
		client.copyObject(bucket, key+"copy", bucket, key);
	}
}
