package com.ksyun.ks3.service;

import static org.junit.Assert.*;

import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.AccessControlPolicy;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.Adp;
import com.ksyun.ks3.dto.AdpInfo;
import com.ksyun.ks3.dto.AdpTask;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.Ks3ObjectSummary;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.ObjectListing;
import com.ksyun.ks3.dto.PostObjectFormFields;
import com.ksyun.ks3.exception.serviceside.AccessDeniedException;
import com.ksyun.ks3.exception.serviceside.QueryTaskFailException;
import com.ksyun.ks3.http.HttpClientFactory;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.service.multipartpost.FormFieldKeyValuePair;
import com.ksyun.ks3.service.multipartpost.HttpPostEmulator;
import com.ksyun.ks3.service.multipartpost.UploadFileItem;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.PutAdpRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.exception.Ks3ClientException;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年2月2日 下午6:18:56
 * 
 * @description 
 **/
public class AdpTest extends Ks3ClientTest{
	final String bucketName = "aaaaaaaavideo";
	final String key = "adpdir/野生动物.3gp";
	final File file = new File("D://野生动物.3gp");
	final File filePut = new File(this.getClass().getClassLoader().getResource("git.exe").toString().substring(6));
	final File logo = new File(this.getClass().getClassLoader().getResource("IMG.jpg").toString().substring(6));
	private static boolean hasUpload = false;
	@Before
	public void createTestBucket(){
		ClientConfig.getConfig().set(ClientConfig.MAX_RETRY,"0");
		
		if(!hasUpload){
			hasUpload = true;
			if(!client.bucketExists(bucketName)){
				client.createBucket(bucketName);
			}else{
				client.clearBucket(bucketName);
			}
			client.putObject(bucketName, "IMG.jpg", logo);
			client.putObject(bucketName,key,file);
		}
	}
	@Test(timeout=1000*2000)
	public void testPutPfop() throws InterruptedException{
		testPutPfop(client);
	}
	@Test(timeout=1000*2000,expected=Ks3ClientException.class)
	public void testPutPfopOtherClient() throws InterruptedException{
		try{
			client.putObjectACL(bucketName,key,CannedAccessControlList.PublicRead);
			testPutPfop(client1);
		}finally{
			client.putObjectACL(bucketName,key,CannedAccessControlList.Private);
		}
	}
	private void testPutPfop(Ks3 client) throws InterruptedException{
		PutAdpRequest request = new PutAdpRequest(bucketName,key);
		List<Adp> fops = new ArrayList<Adp>();
		
		Adp fop1 = new Adp();
		fop1.setCommand("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k");
		fop1.setKey("野生动物-转码.3gp");
		fops.add(fop1);
		
		Adp fop2 = new Adp();
		fop2.setCommand("tag=avop&f=asf&clearmeta=1");
		fop2.setKey("野生动物-清除meta.3gp");
		fops.add(fop2);
		
		Adp fop3 = new Adp();
		fop3.setCommand("tag=avop&f=mp4&ss=5&t=15&vbr=1000k&abr=128k");
		fop3.setKey("野生动物-视频截取.3gp");
		fops.add(fop3);
		
		Adp fop4 = new Adp();
		fop4.setCommand("tag=avop&f=mp4&rotate=90&vbr=1000k&abr=128k&res=1280x720");
		fop4.setKey("野生动物-视频旋转.3gp");
		fops.add(fop4);
		
		Adp fop5 = new Adp();
		fop5.setCommand("tag=avop&f=mp4&vbr=1000k&abr=64k&text=5rWL6K+V5Lit5paH&textlayout=NORTHWEST&font=5b6u6L2v6ZuF6buR&fontcolor=Silver&fontsize=30&textwp=10&texthp=10");
		fop5.setKey("野生动物-文字水印.3gp");
		fops.add(fop5);
		
		Adp fop6 = new Adp();
		fop6.setCommand("tag=avop&f=mp4&vbr=1000k&abr=64k&imgwp=10&imghp=10&imglayout=SOURTHEAST&imgsrc=YWFhYWFhYWF2aWRlbzpJTUcuanBn");
		fop6.setKey("野生动物-图片水印.3gp");
		fops.add(fop6);
		
		Adp fop7 = new Adp();
		fop7.setCommand("tag=avop&f=mp4&an=1");
		fop7.setKey("野生动物-清除音频.3gp");
		fops.add(fop7);
		
		Adp fop8 = new Adp();
		fop8.setCommand("tag=avop&f=mp4&vn=1");
		fop8.setKey("野生动物-清除视频.3gp");
		fops.add(fop8);
		
		Adp fop9 = new Adp();
		fop9.setCommand("tag=avscrnshot&ss=10&res=640x360&rotate=90");
		fop9.setKey("野生动物-视频截图.jpg");
		fops.add(fop9);
		
		Adp fop10 = new Adp();
		fop10.setCommand("tag=avsample&ss=5&t=20&res=640x360&rotate=90&interval=5&pattern=6YeO55Sf5Yqo54mpLemHh+agt+e8qeeVpeWbvi0lM2QuanBn");
		fops.add(fop10);
		
		Adp fop11 = new Adp();
		fop11.setCommand("tag=avconcat&f=flv&mode=2&file=YWFhYWFhYWF2aWRlbzphZHBkaXIv6YeO55Sf5Yqo54mpLjNncA==&loglevel=error");
		fop11.setKey("野生动物-视频拼接.3gp");
		fops.add(fop11);
		
		Adp fop12 = new Adp();
		fop12.setCommand("tag=avm3u8&segtime=10&abr=128k&vbr=1000k&&res=1280x720");
		fop12.setKey("野生动物-hls切片.m3u8");
		fops.add(fop12);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		String id = client.putAdpTask(request).getTaskId();
		AdpTask task;
		while(true){
			task = client.getAdpTask(id);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
	}
	@Test(timeout=1000*600)
	public void testPutPfopWithErrorCommand() throws InterruptedException{
		PutAdpRequest request = new PutAdpRequest(bucketName,key);
		List<Adp> fops = new ArrayList<Adp>();
		
		Adp fop1 = new Adp();
		fop1.setCommand("tag=avop&f=mp41&res=1280x720&vbr=1000k&abr=128k");
		fop1.setKey("野生动物-转码.3gp");
		fops.add(fop1);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		String id = client.putAdpTask(request).getTaskId();
		AdpTask task;
		while(true){
			task = client.getAdpTask(id);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("4",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(false,info.isSuccess());
		}
	}
	@Test(expected=QueryTaskFailException.class)
	public void getPfopWithErrorId(){
		client.getAdpTask("notexist");
	}
	@Test(timeout=1000*600)
	public void putObjectWithPfop() throws InterruptedException{
		putObjectWithPfop(client);
	}
	@Test(timeout=1000*600,expected=Ks3ClientException.class)
	public void putObjectWithPfopOtherClient() throws InterruptedException{
		try{
			client.putBucketACL(bucketName, CannedAccessControlList.PublicReadWrite);
			putObjectWithPfop(client1);
		}finally{
			client.putBucketACL(bucketName, CannedAccessControlList.Private);
		}
	}
	private void putObjectWithPfop(Ks3 client) throws InterruptedException{
		PutObjectRequest request = new PutObjectRequest(bucketName,key,file);
		
		
		List<Adp> fops = new ArrayList<Adp>();
		
		Adp fop1 = new Adp();
		fop1.setCommand("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k");
		fop1.setKey("野生动物-转码.3gp");
		fops.add(fop1);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		
		
		String taskId = client.putObject(request).getTaskid();
		assertNotNull(taskId);
		AdpTask task;
		while(true){
			task = client.getAdpTask(taskId);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
	}
	@Test(timeout=1000*600)
	public void mulitipartUploadWithPfop() throws InterruptedException{
		mulitipartUploadWithPfop(client);
	}
	@Test(timeout=1000*600,expected=Ks3ClientException.class)
	public void mulitipartUploadWithPfopOtherClient() throws InterruptedException{
		try{
			client.putBucketACL(bucketName, CannedAccessControlList.PublicReadWrite);
			mulitipartUploadWithPfop(client1);
		}finally{
			client.putBucketACL(bucketName, CannedAccessControlList.Private);
		}
	}
	private void mulitipartUploadWithPfop(Ks3 client) throws InterruptedException{
		InitiateMultipartUploadRequest initr = new InitiateMultipartUploadRequest(bucketName,key);
		initr.setCannedAcl(CannedAccessControlList.PublicRead);
		InitiateMultipartUploadResult result = client.initiateMultipartUpload(initr);
		
		UploadPartRequest request = new UploadPartRequest(result.getBucket(),result.getKey(),result.getUploadId(),1,file,file.length(),0);
		client.uploadPart(request);	
		
		ListPartsResult parts = super.client.listParts(bucketName, result.getKey(), result.getUploadId());

		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(parts);
		
		List<Adp> fops = new ArrayList<Adp>();
		
		Adp fop1 = new Adp();
		fop1.setCommand("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k");
		fop1.setKey("野生动物-转码.3gp");
		fops.add(fop1);
		
		compRequest.setAdps(fops);
		compRequest.setNotifyURL("http://10.4.2.38:19090/");
		
		String taskId = client.completeMultipartUpload(compRequest).getTaskid();
		
		assertNotNull(taskId);
		AdpTask task;
		while(true){
			task = client.getAdpTask(taskId);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
	}
	@Test(expected=IOException.class)
	public void postObjectPfop() throws Exception{
		Map<String,String> postData = new HashMap<String,String>();

		postData.put("key",key);
		
		PostObjectFormFields fields = client.postObject(bucketName,key, postData,null);
		
		postData.put("policy",fields.getPolicy());
		postData.put("KSSAccessKeyId",fields.getKssAccessKeyId());
		postData.put("signature",fields.getSignature());
		
		String serverUrl = "http://kss.ksyun.com/"+bucketName;//上传地址  
          
	    // 设定要上传的普通Form Field及其对应的value  
	    ArrayList<FormFieldKeyValuePair> ffkvp = new ArrayList<FormFieldKeyValuePair>();  
	    
	    for(Entry<String,String> entry:postData.entrySet()){
	    	ffkvp.add(new FormFieldKeyValuePair(entry.getKey(),entry.getValue()));
	    }
	  
	    // 设定要上传的文件  
	    ArrayList<UploadFileItem> ufi = new ArrayList<UploadFileItem>();  
	    ufi.add(new UploadFileItem("file","D://"+key)); 
	        
	    Map<String,String> headers = new HashMap<String,String>();
	    headers.put(HttpHeaders.AsynchronousProcessingList.toString(),URLEncoder.encode("tag=avop&f=mp4&res=1280x720&vbr=1000k&abr=128k"));
	    headers.put(HttpHeaders.NotifyURL.toString(),"http://10.4.2.38:19090/");
	    HttpPostEmulator hpe = new HttpPostEmulator();  
	    Map<String, List<String>> response = hpe.sendHttpPostRequest(serverUrl, ffkvp, ufi,headers);  
	    System.out.println("Responsefrom server is: " + response);   
	    List<String> taskid = response.get(HttpHeaders.TaskId.toString());
	    assertNotNull(taskid);
	    assertEquals(1,taskid.size());
	    AdpTask task;
		while(true){
			task = client.getAdpTask(taskid.get(0));
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
	}
	@Test
	public void testM3U8() throws InterruptedException, IOException{
		PutAdpRequest request = new PutAdpRequest(bucketName,key);
		List<Adp> fops = new ArrayList<Adp>();
		Adp fop12 = new Adp();
		fop12.setCommand("tag=avm3u8&segtime=10&abr=128k&vbr=1000k&&res=1280x720");
		fop12.setKey("野生动物-hls切片.m3u8");
		fops.add(fop12);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		String id = client.putAdpTask(request).getTaskId();
		AdpTask task;
		while(true){
			task = client.getAdpTask(id);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
		client.headObject(bucketName, "野生动物-hls切片.m3u8");
		client.headObject(bucketName, "野生动物-hls切片.m3u8.ts");
		
		getObjectCommon(bucketName,"野生动物-hls切片.m3u8","野生动物-hls切片.m3u8");
		client.putObjectACL(bucketName, "野生动物-hls切片.m3u8.ts",CannedAccessControlList.PublicRead);
	    
	    FileReader reader = new FileReader(new File("D://"
				+ "野生动物-hls切片.m3u8"));
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		
		List<String> lines = new ArrayList<String>();
		HttpClientFactory factory = new HttpClientFactory();
		HttpClient httpclient = factory.createHttpClient();
		while((line = br.readLine())!=null){
			if(!StringUtils.isBlank(line)&&!line.startsWith("#"))
			{
				lines.add("D://"+line);
				HttpGet get = new HttpGet("http://kss.ksyun.com/"+bucketName+"/"+line);
				HttpResponse response = httpclient.execute(get);
				InputStream input = response.getEntity().getContent();
				putFileCommon(line,input);
				get.abort();
			}
		}
		MultipartUploadByThreads.mergeFiles("D://"+"野生动物-hls切片.m3u8.ts-Down", lines.toArray());
		getObjectCommon(bucketName,"野生动物-hls切片.m3u8.ts","野生动物-hls切片.m3u8.ts");
		assertEquals(Md5Utils.md5AsBase64(new File("D://野生动物-hls切片.m3u8.ts-Down")),
				Md5Utils.md5AsBase64(new File("D://野生动物-hls切片.m3u8.ts")));
	}
	@Test
	public void testBatchOperation() throws InterruptedException{
		PutAdpRequest request = new PutAdpRequest(bucketName,key);
		List<Adp> fops = new ArrayList<Adp>();
		Adp fop12 = new Adp();
		fop12.setCommand("tag=batchoperate&objectkey="+Base64.encodeBase64String("adpdir/".getBytes())+"&acl=public&operation=putobjectacl");
		fops.add(fop12);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		String taskId = client.putAdpTask(request).getTaskId();
		assertNotNull(taskId);
		AdpTask task;
		while(true){
			task = client.getAdpTask(taskId);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
		ObjectListing list = client.listObjects(bucketName, "adpdir/");
		for(Ks3ObjectSummary obj :list.getObjectSummaries()){
			AccessControlPolicy policy = client.getObjectACL(bucketName, obj.getKey());
			assertTrue(2==policy.getAccessControlList().getGrants().size());
		}
		//再设置成私有的
		PutAdpRequest request1 = new PutAdpRequest(bucketName,key);
		List<Adp> fops1 = new ArrayList<Adp>();
		Adp fop121 = new Adp();
		fop121.setCommand("tag=batchoperate&objectkey="+Base64.encodeBase64String("adpdir/".getBytes())+"&acl=private&operation=putobjectacl");
		fops1.add(fop121);
		
		request1.setAdps(fops1);
		request1.setNotifyURL("http://10.4.2.38:19090/");
		String taskId1 = client.putAdpTask(request1).getTaskId();
		assertNotNull(taskId1);
		AdpTask task1;
		while(true){
			task1 = client.getAdpTask(taskId1);
			System.out.println(task1);
			if(task1.isProcessFinished()&&task1.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task1.getProcessstatus());
		assertEquals("1",task1.getNotifystatus());
		for(AdpInfo info :task1.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
		ObjectListing list1 = client.listObjects(bucketName, "adpdir/");
		for(Ks3ObjectSummary obj :list1.getObjectSummaries()){
			AccessControlPolicy policy = client.getObjectACL(bucketName, obj.getKey());
			assertTrue(1==policy.getAccessControlList().getGrants().size());
		}
	}
	@Test
	public void testBatchDelete() throws InterruptedException{
		client.putObject(bucketName, "testdelete/IMG.jpg", logo);
		ObjectListing list = client.listObjects(bucketName, "testdelete/");
		assertTrue(list.getObjectSummaries().size()>0);
		
		PutAdpRequest request = new PutAdpRequest(bucketName,key);
		List<Adp> fops = new ArrayList<Adp>();
		Adp fop12 = new Adp();
		fop12.setCommand("tag=batchoperate&objectkey="+Base64.encodeBase64String("testdelete/".getBytes())+"&operation=del");
		fops.add(fop12);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		String taskId = client.putAdpTask(request).getTaskId();
		assertNotNull(taskId);
		AdpTask task;
		while(true){
			task = client.getAdpTask(taskId);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
		list = client.listObjects(bucketName, "testdelete/");
		assertTrue(0==list.getObjectSummaries().size());
	}
	@Test
	public void testBatchRefresh() throws InterruptedException{
		client.putObject(bucketName, "testdelete/IMG.jpg", logo);
		
		PutAdpRequest request = new PutAdpRequest(bucketName,key);
		List<Adp> fops = new ArrayList<Adp>();
		Adp fop12 = new Adp();
		String url = "http://"+bucketName+".kssws.ks-cdn.com/testdelete/";
		fop12.setCommand("tag=batchoperate&objectkey="+Base64.encodeBase64String("testdelete/".getBytes())+"&operation=refresh&url="+Base64.encodeBase64String(url.getBytes()));
		fops.add(fop12);
		
		request.setAdps(fops);
		request.setNotifyURL("http://10.4.2.38:19090/");
		String taskId = client.putAdpTask(request).getTaskId();
		assertNotNull(taskId);
		AdpTask task;
		while(true){
			task = client.getAdpTask(taskId);
			System.out.println(task);
			if(task.isProcessFinished()&&task.isNotified())
				break;
			Thread.sleep(5000);
		}
		assertEquals("3",task.getProcessstatus());
		assertEquals("1",task.getNotifystatus());
		for(AdpInfo info :task.getAdpInfos()){
			assertEquals(true,info.isSuccess());
		}
	}
	private void getObjectCommon(String bucket,String key,String filename) throws IOException{
		GetObjectResult result = client.getObject(bucket, key);
		InputStream content = result.getObject().getObjectContent();
		
		putFileCommon(filename,content);
	}
	private void putFileCommon(String fileName,InputStream content) throws IOException{
		File file = new File("D://"+ fileName);
		if(file.exists())
			file.delete();
		OutputStream os = new FileOutputStream(new File("D://"
				+ fileName));
		
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
	}
}
