package com.ksyun.ks3.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.AccessControlPolicy;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.HeadObjectResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.PutObjectResult;
import com.ksyun.ks3.dto.ResponseHeaderOverrides;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.exception.serviceside.AccessDeniedException;
import com.ksyun.ks3.exception.serviceside.BucketAlreadyExistsException;
import com.ksyun.ks3.exception.serviceside.NoSuchBucketException;
import com.ksyun.ks3.exception.serviceside.NoSuchKeyException;
import com.ksyun.ks3.exception.serviceside.NotFoundException;
import com.ksyun.ks3.exception.serviceside.SignatureDoesNotMatchException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.Ks3CoreController;
import com.ksyun.ks3.service.request.CopyObjectRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.HeadObjectRequest;
import com.ksyun.ks3.service.request.PutObjectACLRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;

/**
 * @description Tests about Object
 * 				测试内容包括权限测试和权限内功能测试两部分。
 * @author ZHANGZHENGYONG [zhangzhengyong@kingsoft.com]
 * @date 2014年11月17日  	20:30
 */
public class ObjectTest extends ObjectBeforeTest{

	
	private static Log logger = LogFactory.getLog(ObjectTest.class);

	
	
	/**
	 * @tag 功能测试
	 * @Test 错误的 key 或者无效的  bucketName, 抛出  NoSuchKeyException 异常
	 * @expected 抛出  NoSuchKeyException 异常.
	 * @Then {@value 若是输入不存在的bucketName, 是否应该先检查bucket的有效性，给出正确的提示}
	 */
	@Test(expected = NoSuchKeyException.class)
	public void getObjectTest1001(){

		client.getObject(bucket, "hello");

	}
	
	/**
	 * @tag 功能测试
	 * @Test 正确的 key 以及  bucketName
	 * @Then {@value 若是输入内容为文件夹名称 则抛出  NoSuchKeyException 异常, 若输入内容为文件名称则正常返回}
	 */
	@Test(expected = NoSuchKeyException.class)
	public void getObjectTest1002(){
		//输入为文件名，运行正常
		GetObjectResult object = client.getObject(bucket, "hosts.txt");
		System.out.println(object);
		
		//输入为文件夹名， 抛出 NoSuchKeyException
		try{
			client.getObject(bucket, "world");
		}catch(NoSuchKeyException e){
			throw e;
		}
	}
	
	/**
	 * @throws IOException 
	 * @tag 功能测试
	 * @Test 测试文件输出，获取相应文件。
	 * @Then {@value 有时会出现异常}
	 */
	@Test(timeout=20000)
	public void getObjectTest1003() throws IOException {
		GetObjectResult object = client.getObject(bucket, "hosts.txt");
		
		File file = new File(filePath + "/getObjectTest1003.txt");
		FileOutputStream fos = null;

		if (!file.exists()) {
			file.createNewFile();
		}

		AutoAbortInputStream is = object.getObject().getObjectContent();
		fos = new FileOutputStream(file);
		int ch;
		while ((ch = is.read()) != -1) {
			fos.write(ch);
		}
		
		fos.close();
		is.close();
	}
	
	/**
	 * @throws IOException 
	 * @tag 功能测试
	 * @Test 设置range范围，输出指定范围的文件内容
	 * @Then 
	 */
	@Test(timeout=10000)
	public void getObjectTest1004() throws IOException{
		GetObjectRequest request = new GetObjectRequest(bucket, "hosts.txt");
		
		request.setRange(0, 1);
		GetObjectResult object = client.getObject(request);
		
		
		assertEquals(2, object.getObject().getObjectMetadata().getContentLength());
		
		File file = new File(filePath + "/getObjectTest1004.txt");

		FileOutputStream fos = null;
		
		if(!file.exists()){
			file.createNewFile();
		}
		fos = new FileOutputStream(file);
		AutoAbortInputStream is = object.getObject().getObjectContent();
		int ch;
		while((ch = is.read())!=-1){
			fos.write(ch);
		}
		
		is.close();
		fos.close();
		System.out.println(object);
	}
	
	/**
	 * @tag 功能测试
	 * @DoTask
	 * @Test 关于modify属性的测试 
	 * @Then 
	 */
	@Test()
	public void getObjectTest1005(){
		GetObjectRequest request = new GetObjectRequest(bucket, "hosts.txt");
		
		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.YEAR, -5);
		request.setModifiedSinceConstraint(cal.getTime());
//		request.setUnmodifiedSinceConstraint(cal.getTime());
		
		GetObjectResult object = client.getObject(request);
		
		System.out.println(cal.getTime());
//		assertTrue(object.isIfModified());
		assertTrue(object.isIfPreconditionSuccess());
	}
	
	/**
	 * @tag 权限测试
	 * @Test 客户端访问非本用户文件---公共文件
	 * @Then 
	 */
	@Test
	public void getObjectTest1006(){
		client.putObjectACL(bucket, "hostsPulbic.txt", CannedAccessControlList.PublicReadWrite);
		GetObjectResult object = clientOther.getObject(bucket, "hostsPulbic.txt");
		System.out.println(object);
	}
	
	
	/**
	 * @tag 权限测试
	 * @Test 客户端访问非本用户文件---私密文件
	 * @Then 
	 */
	@Test(expected=AccessDeniedException.class)
	public void getObjectTest1007(){
		GetObjectResult object = clientOther.getObject(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	/**
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @tag 功能测试  GET Object
	 * @Test 
	 * @Then 
	 */
	@Test()
	public void getObjectTest1008() throws IllegalArgumentException, IllegalAccessException{
		GetObjectRequest request = new GetObjectRequest(bucket, "hosts.txt");
		ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
		overrides.setCacheControl("no-cache");
		overrides.setContentDisposition("D:/");
		//overrides.setContentEncoding("gb2312");
		/*overrides.setContentLanguage("En");*/
		overrides.setContentType("txt");
		overrides.setExpires(new Date());
		request.setOverrides(overrides);
		GetObjectResult object = client.getObject(request);
		
//		HeadObjectResult result = client.headObject(bucket, "hosts.txt");
		
		ObjectMetadata metas = object.getObject().getObjectMetadata();
		
		System.out.println(metas.getCacheControl());
		System.out.println(metas.getContentDisposition());
		System.out.println(metas.getContentEncoding());
		System.out.println(metas.getContentType());
		System.out.println(metas.getHttpExpiresDate());
//		System.out.println("abc\n" + result.getObjectMetadata());
		
//		System.out.println(object);
	}
	
	
	/**
	 * @tag 测试  GET Object
	 * @Test 
	 * @Then 
	 */
	@Test()
	public void getObjectTest(){
		GetObjectResult object = client.getObject(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	
	/**
	 * @tag 功能测试
	 * @Test 错误的 key 或者无效的  bucketName, 抛出  NoSuchKeyException 异常
	 * @expected 抛出  Ks3ServiceException 异常.
	 * @Then 
	 */
	@Test(expected = NotFoundException.class)
	public void headObjectTest2001(){
		
		client.headObject(bucket, "hello");
//		client.headObject("123", "hello");

	}
	
	/**
	 * @tag 功能测试
	 * @Test 正确的 key 以及  bucketName
	 * @Then {@value 若是输入内容为文件夹名称 则抛出  Ks3ServiceException 异常, 若输入内容为文件名称则正常返回}
	 */
	@Test(expected = Ks3ServiceException.class)
	public void headObjectTest2002(){
		boolean flag = true;//true 代表为文件名，false 代表文件夹名
		if(flag){
			//输入为文件名，运行正常
			client.headObject(bucket, "hosts.txt");
			throw new NoSuchKeyException();
		} else {
			// 输入为文件夹名， 抛出 NoSuchKeyException
			try {
				client.headObject(bucket, "world");
			} catch (NoSuchKeyException e) {
				throw e;
			}
		}
	}
	
	/**
	 * @tag 功能测试
	 * @Test 测试内容，显示输出。
	 * @Then 
	 */
	@Test(timeout=5000)
	public void headObjectTest2003() {
		HeadObjectResult object = client.headObject(bucket, "hosts.txt");
		
		System.out.println(object);
	}
	
	/**
	 * @throws IOException 
	 * @tag 功能测试
	 * @Test 设置range范围，响应头部Content-Length与设置的值一致。
	 * @Then 
	 */
	@Test(timeout=5000)
	public void headObjectTest2004() throws IOException{
		GetObjectResult ret1 = client.getObject(bucket, "hosts.txt");
		long length = ret1.getObject().getObjectMetadata().getContentLength();
		
		HeadObjectRequest request = new HeadObjectRequest(bucket, "hosts.txt");
		
		request.setRange(0, 2);
		
		HeadObjectResult result = client.headObject(request);
		
		assertEquals(3,result.getObjectMetadata().getContentLength());
		
		
		
		HeadObjectRequest request1 = new HeadObjectRequest(bucket, "hosts.txt");
		
		request1.setRange(1, 2);
		
		HeadObjectResult result1 = client.headObject(request1);
		
		assertEquals(2,result1.getObjectMetadata().getContentLength());
		
		
		
		HeadObjectRequest request2 = new HeadObjectRequest(bucket, "hosts.txt");
		
		request2.setRange(1,length+10);
		
		HeadObjectResult result2 = client.headObject(request2);
		
		assertEquals(length-1,result2.getObjectMetadata().getContentLength());
	}
	
	/**
	 * @tag 功能测试
	 * @DoTask
	 * @Test 关于部分属性的测试 
	 * @Then 
	 */
	@Test()
	public void headObjectTest2005(){
		HeadObjectRequest request = new HeadObjectRequest(bucket, "hosts.txt");
		
		Calendar cal = Calendar.getInstance();
		request.setModifiedSinceConstraint(cal.getTime());
		request.setUnmodifiedSinceConstraint(cal.getTime());
		
		HeadObjectResult object = client.headObject(request);
		
		assertFalse(object.isIfModified());
		assertTrue(object.isIfPreconditionSuccess());
	}
	
	/**
	 * @tag 权限测试
	 * @Test 客户端访问非本用户文件---公共文件
	 * @Then 
	 */
	@Test
	public void headObjectTest2006(){
		HeadObjectResult object = clientOther.headObject(bucket, "hostsPulbic.txt");
		System.out.println(object);
	}
	
	
	/**
	 * @tag 权限测试
	 * @Test 客户端访问非本用户文件---私密文件
	 * @Then
	 */
	@Test(expected=AccessDeniedException.class)
	public void headObjectTest2007(){
		HeadObjectResult object = clientOther.headObject(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 测试 HEAD Object
	 * @Test 错误的bucketName， 抛出 Ks3ServiceException 异常
	 * @Then {@value 是否应该抛出 NoSuchBucketException}
	 */
	@Test(expected=Ks3ServiceException.class)
	public void headObjectTest2008(){
		String noBucket = "notexist";
		HeadObjectResult object = client.headObject(noBucket, "hosts.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 测试 HEAD Object
	 * @Test 
	 * @Then 
	 */
	@Test
	public void headObjectTest(){
		HeadObjectResult object = client.headObject(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	
	/**
	 * @tag 功能测试
	 * @Test 正确的 key 以及  bucketName
	 * @Then 
	 */
	@Test
	public void getObjectACLTest3001(){
		AccessControlPolicy object = client.getObjectACL(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 功能测试
	 * @Test 错误的 key, 抛出  NoSuchKeyException 异常
	 * @Then 
	 */
	@Test(expected = NoSuchKeyException.class)
	public void getObjectACLTest3002(){
		AccessControlPolicy object = client.getObjectACL(bucket, "host.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 功能测试
	 * @Test 错误的 bucket, 抛出  NoSuchBucketException 异常
	 * @Then 
	 */
	@Test(expected = NoSuchBucketException.class)
	public void getObjectACLTest3003(){
		String tempbucket = "notexists"+System.currentTimeMillis();
		AccessControlPolicy object = client.getObjectACL(tempbucket, "host.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 权限测试 
	 * @Test 客户端访问非本用户文件---公开文件
	 * @Then 
	 */
	@Test()
	public void getObjectACLTest3004(){

		AccessControlPolicy object = clientOther.getObjectACL(bucket, "hostsPulbic.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 权限测试 
	 * @Test 客户端访问非本用户文件---私密文件
	 * @Then 
	 */
	@Test(expected=AccessDeniedException.class)
	public void getObjectACLTest3005(){

		AccessControlPolicy object = clientOther.getObjectACL(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	/**
	 * @tag 测试  GET Object ACL
	 * @Test 打印对象内容
	 * @Then 
	 */
	@Test()
	public void getObjectACLTest(){

		AccessControlPolicy object = client.getObjectACL(bucket, "hosts.txt");
		System.out.println(object);
	}
	
	
	/**
	 * @tag 功能测试	 Delete Object
	 * @Test 正确的objectKey, bucket， 删除对象。
	 * @Then 
	 */
	@Test
	public void deleteObjectTest4001(){
		
		try{
			client.putObject(bucket, "deleteTestP.txt", new File(filePath + "/deleteTestP.txt"));
		}catch(Exception e){}
		
		client.deleteObject(bucket, "deleteTestP.txt");
		client.putObject(bucket, "deleteTestP.txt", new File(filePath + "/deleteTestP.txt"));
		
	}
	
	/**
	 * @tag 功能测试	 Delete Object
	 * @Test 错误的 objectKey， 删除对象。
	 * @Then 
	 */
	@Test(expected=NoSuchKeyException.class)
	public void deleteObjectTest4002(){

		client.deleteObject(bucket, "deleteTestNull.txt");
		
	}
	
	/**
	 * @tag 功能测试	 Delete Object
	 * @Test 错误的 bucket， 删除对象。
	 * @Then 
	 */
	@Test(expected=NoSuchBucketException.class)
	public void deleteObjectTest4003(){
		String noBucket = "notexist";
		client.deleteObject(noBucket, "deleteTest.txt");
		
	}
	
	/**
	 * @tag 权限测试	 Delete Object
	 * @Test 客户端删除非本用户文件---公开文件
	 * @Then 
	 */
	@Test(expected=AccessDeniedException.class)
	public void deleteObjectTest4004(){
		client.putObjectACL(bucket, "deleteTestP.txt", CannedAccessControlList.PublicReadWrite);
		clientOther.deleteObject(bucket, "deleteTestP.txt");
		client.putObject(bucket, "deleteTestP.txt", new File(filePath + "/deleteTestP.txt"));
	}
	
	/**
	 * @tag 权限测试	 Delete Object
	 * @Test 客户端删除非本用户文件---私密文件
	 * @Then 
	 */
	@Test(expected=NoSuchKeyException.class)
	public void deleteObjectTest4005(){

		clientOther.deleteObject(bucket, "deleteTest.txt");
		
	}
	
	/**
	 * @tag 测试 Delete Object
	 * @Test 
	 * @Then 
	 */
	@Test()
	public void deleteObjectTest(){

	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，正确的文件名  上传文件
	 * @Then 
	 */
	@Test()
	public void putObjectTest5001(){
		PutObjectResult result = client.putObject(bucket, "/putObjectTest.txt",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，错误的文件名， 上传文件
	 * @Then 
	 */
	@Test(expected=Ks3ClientException.class)
	public void putObjectTest5002(){
		PutObjectResult result = client.putObject(bucket, "/putObjectTest.txt",new File(filePath + "/notexist.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 错误的bucket，正确的文件名， 上传文件
	 * @Then 
	 */
	@Test(expected=NoSuchBucketException.class)
	public void putObjectTest5003(){
		String noBucket = "notexist";
		PutObjectResult result = client.putObject(noBucket, "/putObjectTest.txt",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，正确的文件名， 设置公开或私有属性，上传文件
	 * @Then 
	 */
	@Test()
	public void putObjectTest5004(){
		List<String> resultList = new ArrayList<String>();
		PutObjectRequest request = new PutObjectRequest(bucket, "//putObjectTest.txt", new File(filePath + "/putObjectTest.txt"));
		request.setCannedAcl(CannedAccessControlList.Private);
		resultList.add(request.getCannedAcl() + ":" + client.putObject(request));
		
		request = new PutObjectRequest(bucket, "//putObjectTestP.txt", new File(filePath + "/putObjectTestP.txt"));
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		resultList.add(request.getCannedAcl() + ":" + client.putObject(request));
		
//		request.setCannedAcl(CannedAccessControlList.PublicReadWrite);
//		resultList.add(request.getCannedAcl() + ":" + client.putObject(request));
		
		for(String result:resultList){
			System.out.println(result);
		}
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，文件名为".."  上传文件
	 * @Then {@value Ks3ServiceException 异常}
	 */
	@Test(expected=Ks3ServiceException.class)
	public void putObjectTest5006(){
		PutObjectResult result = client.putObject(bucket, "..",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，文件名为"。"  上传文件
	 * @Then 
	 */
	@Test()
	public void putObjectTest5007(){
		PutObjectResult result = client.putObject(bucket, "。",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，文件名为"。。"  上传文件
	 * @Then 
	 */
	@Test()
	public void putObjectTest5008(){
		PutObjectResult result = client.putObject(bucket, "。。",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，文件名为"/./putObjectTest.txt"  上传文件
	 * @Then 
	 */
	@Test
	public void putObjectTest5009(){
		PutObjectResult result = client.putObject(bucket, "/./putObjectTest.txt",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 正确的bucket，文件名为"/.../putObjectTest.txt"  上传文件
	 * @Then 
	 */
	@Test
	public void putObjectTest5011(){
		PutObjectResult result = client.putObject(bucket, "/.../putObjectTest.txt",new File(filePath + "/putObjectTest.txt"));
		System.out.println(result);
		
	}
	
	/**
	 * @tag 测试	 PUT Object
	 * @Test 
	 * @expected objectMeta 一致
	 * @Then 
	 */
	@Test
	public void putObjectTest5012() {
		PutObjectRequest request = new PutObjectRequest(bucket, "putObjectTestMeta.txt", new File(filePath + "/putObjectTest.txt"));
		
		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setContentType("txt");
		objectMeta.setContentMD5("ruzU5HevHvJyciYjskBwbw=");
		objectMeta.setContentDisposition("D:/abc/");
		objectMeta.setHttpExpiresDate(new Date());
		objectMeta.setContentEncoding("gb2312");
		objectMeta.setCacheControl("abc");
		request.setObjectMeta(objectMeta);
		
		client.putObject(request);
		
		GetObjectResult result = client.getObject(bucket,"putObjectTestMeta.txt");
		ObjectMetadata objectMetaGet = result.getObject().getObjectMetadata();
		System.out.println(objectMetaGet);
		
		assertEquals(objectMeta.getContentType(), objectMetaGet.getContentType());
//		assertEquals(objectMeta.getContentDisposition(), objectMetaGet.getContentDisposition());
		System.out.println(objectMeta);
	}
	
	/**
	 * @tag 权限测试	 PUT Object
	 * @Test 客户端向非本用户bucket添加文件
	 * @Then 
	 */
	@Test(expected=AccessDeniedException.class)
	public void putObjectTest5013() throws FileNotFoundException{
		PutObjectRequest request = new PutObjectRequest(bucket, "/putObjectTest1.txt", new File(filePath + "/putObjectTest.txt"));
//		request.setCannedAcl(CannedAccessControlList.Private);
		PutObjectResult result = clientOther.putObject(request);
		System.out.println(result);
	}
	
	/**
	 * @tag 功能测试	 PUT Object
	 * @Test 设置objectMeta中的UserMeta
	 * @Then 
	 */
	@Test()
	public void putObjectTest5014(){
		PutObjectRequest request = new PutObjectRequest(bucket, "putObjectTestUserMeta.txt", new File(filePath + "/putObjectTest.txt"));
		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setUserMeta("abc", "abc");
		objectMeta.setUserMeta("cdd", "1131445");
		
		request.setObjectMeta(objectMeta);
		
		client.putObject(request);
		
		GetObjectResult result = client.getObject(bucket, "putObjectTestUserMeta.txt");
		System.out.print(result.getObject().getObjectMetadata());
		
		System.out.print(objectMeta);
		
	}
	
	/**
	 * @tag 测试	 PUT Object
	 * @Test 
	 * @Then 
	 */
	@Test()
	public void putObjectTest(){
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object ACL
	 * @Test 正确的bucket, 正确的objectkey
	 * @Then 
	 */
	@Test()
	public void putObjectACLTest6001(){
		PutObjectACLRequest request = new PutObjectACLRequest(bucket, "putObjectTest.txt");
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTestP.txt").getAccessControlList();
		request.setAccessControlList(acl);
		client.putObjectACL(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object ACL
	 * @Test 正确的bucket, 错误的objectKey
	 * @Then 
	 */
	@Test(expected=NoSuchKeyException.class)
	public void putObjectACLTest6002(){
		PutObjectACLRequest request = new PutObjectACLRequest(bucket, "notexist");
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTestP.txt").getAccessControlList();
		request.setAccessControlList(acl);
		client.putObjectACL(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object ACL
	 * @Test 错误的bucket, 正确的objectKey
	 * @Then 
	 */
	@Test(expected=NoSuchBucketException.class)
	public void putObjectACLTest6003(){
		String tempBucket = "notexist";
		PutObjectACLRequest request = new PutObjectACLRequest(tempBucket, "putObjectTest.txt");
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTestP.txt").getAccessControlList();
		request.setAccessControlList(acl);
		client.putObjectACL(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object ACL
	 * @Test 正确的bucket, 正确的objectKey, 将某个文件的 acl 赋予另外一个文件。
	 * @Then {@value acl 不一致，统一用户操作，不同文件}
	 */
	@Test
	public void putObjectACLTest6004(){
		List<String> aclList = new ArrayList<String>();
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTestP.txt").getAccessControlList();
		aclList.add("putObjectTestP.txt: " + acl.toString());
		
		PutObjectACLRequest request = new PutObjectACLRequest(bucket, "putObjectTest.txt", acl);
		client.putObjectACL(request);
		
		AccessControlList acl1 = client.getObjectACL(bucket, "putObjectTest.txt").getAccessControlList();
		aclList.add("putObjectTest.txt: " + acl1.toString());
		
		
//		assertEquals(acl, acl1);
		for(String aclStr:aclList){
			System.out.print(aclStr);
		}
	}
	
	/**
	 * @tag 功能测试	 PUT Object ACL
	 * @Test 正确的bucket, 正确的objectKey, 将某个文件的 acl 赋予另外一个文件，同时设置文件Canned权限。
	 * @Then 
	 */
	@Test
	public void putObjectACLTest6005(){
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTest.txt").getAccessControlList();
		
		
		PutObjectACLRequest request = new PutObjectACLRequest(bucket, "putObjectTestP1.txt", acl, CannedAccessControlList.Private);
		client.putObjectACL(request);
		
		AccessControlList acl1 = client.getObjectACL(bucket, "putObjectTestP1.txt").getAccessControlList();
		
//		assertEquals(acl, acl1);
	}
	
	/**
	 * @tag 权限测试	 PUT Object ACL
	 * @Test 客户端将某个文件的 acl 赋予另外一个非本用户文件---公共文件
	 * @Then 
	 */
	@Test(expected=AccessDeniedException.class, timeout=2000)
	public void putObjectACLTest6006(){
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTestP1.txt").getAccessControlList();
		
		PutObjectACLRequest request = new PutObjectACLRequest(bucket, "putObjectTestP.txt", acl);
		clientOther.putObjectACL(request);
		
		
	}
	
	/**
	 * @tag 权限测试	 PUT Object ACL
	 * @Test 客户端将某个文件的 acl 赋予另外一个非本用户文件---私密文件
	 * @expected AccessDeniedException
	 * @Then 
	 */
	@Test(expected=AccessDeniedException.class)
	public void putObjectACLTest6007(){
		AccessControlList acl = client.getObjectACL(bucket, "putObjectTestP.txt").getAccessControlList();
		
		PutObjectACLRequest request = new PutObjectACLRequest(bucket, "putObjectTest.txt", acl);
		clientOther.putObjectACL(request);
		
	}
	
	/**
	 * @tag 测试	 PUT Object ACL
	 * @Test 
	 * @Then 
	 */
	@Test()
	public void putObjectACLTest(){
		
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中
	 * @expected 
	 * @Then 
	 */
	@Test
	public void copyObjectTest7001(){
		String desBucket = bucketOther;
		String desObject = "abc.txt";
		try{
			if(client.getObject(desBucket, desObject).getObject().getObjectContent()!=null){
				client.deleteObject(desBucket, desObject);
			}
		}catch(Exception e){/*屏蔽异常 */}
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
		client.copyObject(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中，已存在desObject
	 * @expected Ks3ServiceException 异常  Error code:invalidkey
	 * @Then 
	 */
	@Test(expected=Ks3ServiceException.class)
	public void copyObjectTest7002(){
		String desBucket = bucketOther;
		String desObject = "abc.txt";
		if(client.getObject(desBucket, desObject).getObject()==null){
			CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
			client.copyObject(request);
		}
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
		client.copyObject(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中, destinationBucket不存在
	 * @expected NoSuchBucketException
	 * @Then 
	 */
	@Test(expected=NoSuchBucketException.class)
	public void copyObjectTest7003(){
		String desBucket = "test100-zzy";
		String desObject = "abc.txt";
		try {
			if (client.getObject(desBucket, desObject).getObject() != null) {
				client.deleteObject(desBucket, desObject);
			}
		} catch (Exception e) {// 屏蔽异常，方便测试
		}
		
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
		client.copyObject(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中， sourceBucket不存在
	 * @expected NoSuchBucketException
	 * @Then 
	 */
	@Test(expected=NoSuchBucketException.class)
	public void copyObjectTest7004(){
		String desBucket = bucketOther;
		String desObject = "abc.txt";
		try {
			if (client.getObject(desBucket, desObject).getObject() != null) {
				client.deleteObject(desBucket, desObject);
			}
		} catch (Exception e) {// 屏蔽异常，方便测试
		}
		String tempBucket = "notexist";
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, tempBucket, "hosts.txt");
		client.copyObject(request);
	}
	
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中， sourceObject不存在
	 * @expected NoSuchKeyException
	 * @Then 
	 */
	@Test(expected=NoSuchKeyException.class)
	public void copyObjectTest7005(){
		String desBucket = bucketOther;
		String desObject = "abc.txt";
		try {
			if (client.getObject(desBucket, desObject).getObject() != null) {
				client.deleteObject(desBucket, desObject);
			}
		} catch (Exception e) {// 屏蔽异常，方便测试
		}
		
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "notexist1");
		client.copyObject(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中，设定Canned为公开
	 * @expected 权限为公开
	 * @Then 
	 */
	@Test()
	public void copyObjectTest7006(){
		String desBucket = bucketOther;
		String desObject = "abc12.txt";
		try {
			if (client.getObject(desBucket, desObject).getObject() != null) {
				client.deleteObject(desBucket, desObject);
			}
		} catch (Exception e) {// 屏蔽异常，方便测试
		}
		
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
		request.setCannedAcl(CannedAccessControlList.PublicReadWrite);
		client.copyObject(request);
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从sourceBucket 拷贝 object 到 destinationBucket中，设定accessControlList
	 * @expected 
	 * @Then {@value NoSuchKeyException}
	 */
	@Test(expected=Ks3ServiceException.class)
	public void copyObjectTest7007(){
		String desBucket = bucketOther; 
		String desObject = "abc88.txt";
		
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
		request.setCannedAcl(CannedAccessControlList.PublicReadWrite);
		AccessControlList acl1 = client.getObjectACL(bucket, "hosts.txt").getAccessControlList();
		request.setAccessControlList(acl1);
		client.copyObject(request);
		client.copyObject(request);
		
		AccessControlList acl2 = client.getObjectACL(desBucket, desObject).getAccessControlList();
		System.out.println("acl1:"+acl1+"\nacl2:"+acl2);
	}
	
	/**
	 * @tag 功能测试	 PUT Object COPY
	 * @Test 从非本用户sourceBucket 拷贝 object 到 destinationBucket中
	 * @expected 
	 * @Then {@value 因数据假删除，所造成无法上传同文件名的文件}
	 */
	@Test(expected=AccessDeniedException.class)
	public void copyObjectTest7008(){
		String desBucket = bucketOther;
		String desObject = "abc13.txt";
		try{
			if(client.getObject(desBucket, desObject).getObject()!=null){
				client.deleteObject(desBucket, desObject);
			}
		}catch(Exception e){/*屏蔽异常 */}
		CopyObjectRequest request = new CopyObjectRequest(desBucket, desObject, bucket, "hosts.txt");
		clientOther.copyObject(request);
	}
	
	/**
	 * @tag 测试	 PUT Object COPY
	 * @Test 
	 * @Then 
	 */
	@Test()
	public void copyObjectTest(){
		
	}
	
}
