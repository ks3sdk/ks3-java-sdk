package com.ksyun.ks3.unit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import com.ksyun.ks3.dto.Bucket;
import com.ksyun.ks3.dto.BucketLoggingStatus;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.ListPartsResult;
import com.ksyun.ks3.dto.ObjectListing;
import com.ksyun.ks3.dto.Part;
import com.ksyun.ks3.dto.ResponseHeaderOverrides;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.HttpClientFactory;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.service.request.GeneratePresignedUrlRequest;
import com.ksyun.ks3.service.response.CompleteMultipartUploadResponse;
import com.ksyun.ks3.service.response.CreateBucketResponse;
import com.ksyun.ks3.service.response.DeleteBucketResponse;
import com.ksyun.ks3.service.response.DeleteObjectResponse;
import com.ksyun.ks3.service.response.GetBucketACLResponse;
import com.ksyun.ks3.service.response.GetBucketLoggingResponse;
import com.ksyun.ks3.service.response.GetObjectResponse;
import com.ksyun.ks3.service.response.HeadBucketResponse;
import com.ksyun.ks3.service.response.HeadObjectResponse;
import com.ksyun.ks3.service.response.InitiateMultipartUploadResponse;
import com.ksyun.ks3.service.response.Ks3WebServiceResponse;
import com.ksyun.ks3.service.response.ListBucketsResponse;
import com.ksyun.ks3.service.response.ListObjectsResponse;
import com.ksyun.ks3.service.response.ListPartsResponse;
import com.ksyun.ks3.service.response.PutBucketACLResponse;
import com.ksyun.ks3.service.response.PutBucketLoggingResponse;
import com.ksyun.ks3.service.response.PutObjectResponse;
import com.ksyun.ks3.service.response.UploadPartResponse;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlWriter;

public class TestPresignedUrl extends BaseTest{
	
	@Test
	public void testListBuckets() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.GET);
		String url = client.generatePresignedUrl(req);
		HttpGet httpReq = new HttpGet(url);
		List<Bucket> buckets = handle(httpReq,ListBucketsResponse.class);
		boolean found =false;
		for(Bucket b:buckets){
			if(b.getName().equals(bucket))
				found =true;
		}
		assertTrue(found);
	}
	@Test
	public void testHeadBucket() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.HEAD);
		req.setBucket(bucket);
		String url = client.generatePresignedUrl(req);
		
		HttpHead httpReq = new HttpHead(url);
		handle(httpReq,HeadBucketResponse.class);
	}
	@Test
	public void testDeleteBucket() throws ClientProtocolException, IOException {
		client.putObject(bucket, key, "123");
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.DELETE);
		req.setBucket(bucket);
		String url = client.generatePresignedUrl(req);
		
		HttpDelete httpReq = new HttpDelete(url);
		handle(httpReq,DeleteBucketResponse.class,409);
	}
	@Test
	public void testGetBucketAcl() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.GET);
		req.setBucket(bucket);
		req.getRequestParameters().put("acl", null);
		String url = client.generatePresignedUrl(req);
		
		HttpGet httpReq = new HttpGet(url);
		handle(httpReq,GetBucketACLResponse.class);
	}
	@Test
	public void testPutBucket() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.PUT);
		req.setBucket(bucket);
		String url = client.generatePresignedUrl(req);
		
		HttpPut httpReq = new HttpPut(url);
		handle(httpReq,CreateBucketResponse.class,409);
	}
	@Test
	public void testPutBucketAcl() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.PUT);
		req.setBucket(bucket);
		req.getRequestParameters().put("acl",null);
		req.getRequestConfig().getExtendHeaders().put("x-kss-acl","public-read");
		String url = client.generatePresignedUrl(req);
		
		HttpPut httpReq = new HttpPut(url);
		httpReq.addHeader("x-kss-acl","public-read");
		handle(httpReq,PutBucketACLResponse.class);
		assertEquals(CannedAccessControlList.PublicRead,client.getBucketCannedACL(bucket));
	}
	@Test
	public void testListObjects() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.GET);
		req.setBucket(bucket);
		String url = client.generatePresignedUrl(req);
		
		HttpGet httpReq = new HttpGet(url);
		ObjectListing list = handle(httpReq,ListObjectsResponse.class);
		assertEquals(bucket,list.getBucketName());
	}
	@Test
	public void testPutAndGetBucketLogging() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.PUT);
		req.setBucket(bucket);
		req.getRequestParameters().put("logging",null);
		req.getRequestConfig().getExtendHeaders().put("Content-Type","application/xml");
		String url = client.generatePresignedUrl(req);
		
		XmlWriter writer = new XmlWriter();
		writer.startWithNs("BucketLoggingStatus").start("LoggingEnabled").start("TargetBucket").value(bucket).end().start("TargetPrefix").value("").end().end().end();
		HttpEntity entity = new StringEntity(writer.toString());
		HttpPut httpReq = new HttpPut(url);
		httpReq.addHeader("Content-Type","application/xml");
		httpReq.setEntity(entity);
		handle(httpReq,PutBucketLoggingResponse.class);

		GeneratePresignedUrlRequest getReq = new GeneratePresignedUrlRequest();
		getReq.setMethod(HttpMethod.GET);
		getReq.setBucket(bucket);
		getReq.getRequestParameters().put("logging",null);
		String getUrl = client.generatePresignedUrl(getReq);
		HttpGet httpReqG = new HttpGet(getUrl);
		BucketLoggingStatus logging = handle(httpReqG,GetBucketLoggingResponse.class);
		assertTrue(logging.isEnable());
		assertEquals(bucket,logging.getTargetBucket());
		assertEquals("",logging.getTargetPrefix());
	}
	@Test
	public void testDeleteObject() throws ClientProtocolException, IOException{
		client.putObject(bucket, key, "123");
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.DELETE);
		req.setBucket(bucket);
		req.setKey(key);
		String url = client.generatePresignedUrl(req);
		
		HttpDelete httpReq = new HttpDelete(url);
		handle(httpReq,DeleteObjectResponse.class);
		assertFalse(client.objectExists(bucket, key));
	}
	@Test
	public void testGetObject() throws ClientProtocolException, IOException{
		client.putObject(bucket, key, "123");
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.GET);
		req.setBucket(bucket);
		req.setKey(key);
		ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
		overrides.setContentType("application/xml");
		req.setResponseHeaders(overrides);
		String url = client.generatePresignedUrl(req);
		
		HttpGet httpReq = new HttpGet(url);
		GetObjectResult getRet = handle(httpReq,GetObjectResponse.class);
		assertEquals("123",StringUtils.inputStream2String(getRet.getObject().getObjectContent()));
		assertEquals("application/xml",getRet.getObject().getObjectMetadata().getContentType());	
	}
	@Test
	public void testHeadObject() throws ClientProtocolException, IOException{
		client.putObject(bucket, key, "123");
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.HEAD);
		req.setBucket(bucket);
		req.setKey(key);
		String url = client.generatePresignedUrl(req);
		
		HttpHead httpReq = new HttpHead(url);
		handle(httpReq,HeadObjectResponse.class);
	}
	@Test
	public void testPutObject() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest();
		req.setMethod(HttpMethod.PUT);
		req.setBucket(bucket);
		req.setKey(key);
		req.getRequestConfig().getExtendHeaders().put("x-kss-acl", "public-read");
		req.setContentType("application/pdf");
		req.setSseAlgorithm("AES256");
		String url = client.generatePresignedUrl(req);
		
		HttpPut httpReq = new HttpPut(url);
		httpReq.addHeader("x-kss-acl","public-read");
		httpReq.addHeader("Content-Type", "application/pdf");
		httpReq.addHeader("x-kss-server-side-encryption","AES256");
		httpReq.setEntity(new StringEntity("1234"));
		handle(httpReq,PutObjectResponse.class);
		
		assertTrue(client.objectExists(bucket, key));
		assertEquals(CannedAccessControlList.PublicRead,client.getObjectCannedACL(bucket, key));
	}
	@Test
	public void testMultiPartUpload() throws ClientProtocolException, IOException{
		GeneratePresignedUrlRequest initReq = new GeneratePresignedUrlRequest();
		initReq.setMethod(HttpMethod.POST);
		initReq.setBucket(bucket);
		initReq.setKey(key);
		initReq.getRequestParameters().put("uploads", null);
		String initUrl = client.generatePresignedUrl(initReq);
		HttpPost initHReq = new HttpPost(initUrl);
		InitiateMultipartUploadResult initRet = handle(initHReq,InitiateMultipartUploadResponse.class);
		assertNotNull(initRet.getUploadId());
		
		GeneratePresignedUrlRequest upReq = new GeneratePresignedUrlRequest();
		upReq.setMethod(HttpMethod.PUT);
		upReq.setBucket(bucket);
		upReq.setKey(key);
		upReq.setContentType("application/ocet-stream");
		upReq.getRequestParameters().put("partNumber","1");
		upReq.getRequestParameters().put("uploadId",initRet.getUploadId());
		String upUrl = client.generatePresignedUrl(upReq);
		HttpPut upHReq = new HttpPut(upUrl);
		upHReq.addHeader("Content-Type", "application/ocet-stream");
		upHReq.setEntity(new StringEntity("123"));
		handle(upHReq,UploadPartResponse.class);
		
		GeneratePresignedUrlRequest liReq = new GeneratePresignedUrlRequest();
		liReq.setMethod(HttpMethod.GET);
		liReq.setBucket(bucket);
		liReq.setKey(key);
		liReq.getRequestParameters().put("uploadId",initRet.getUploadId());
		String liUrl = client.generatePresignedUrl(liReq);
		HttpGet liHReq = new HttpGet(liUrl);
		ListPartsResult liRet = handle(liHReq,ListPartsResponse.class);
		
		GeneratePresignedUrlRequest comReq = new GeneratePresignedUrlRequest();
		comReq.setMethod(HttpMethod.POST);
		comReq.setBucket(bucket);
		comReq.setKey(key);
		comReq.setContentType("application/ocet-stream");
		comReq.getRequestParameters().put("uploadId",initRet.getUploadId());
		String comUrl = client.generatePresignedUrl(comReq);
		XmlWriter writer = new XmlWriter();
		writer.start("CompleteMultipartUpload");
		for(Part p:liRet.getParts()){
			writer.start("Part").start("PartNumber").value(p.getPartNumber()).end().start("ETag").value(p.getETag()).end().end();
		}
		writer.end();
		HttpPost comHReq = new HttpPost(comUrl);
		comHReq.setEntity(new StringEntity(writer.toString()));
		comHReq.addHeader("Content-Type", "application/ocet-stream");
		handle(comHReq,CompleteMultipartUploadResponse.class);
		
		assertTrue(client.objectExists(bucket, key));
	}
	public  <X extends Ks3WebServiceResponse<Y>, Y> Y handle(HttpUriRequest req,Class<X> clazz,int status) throws ClientProtocolException, IOException{
		HttpResponse response = hclient.execute(req);
		if(status == 0){
			assertEquals(2,response.getStatusLine().getStatusCode()/100);
		}else{
			assertEquals(status,response.getStatusLine().getStatusCode());
		}
		Ks3WebServiceResponse<Y> ksResponse = null;
		try {
			ksResponse = clazz.newInstance();
		} catch (InstantiationException e) {
			// 正常情况不会抛出
			throw new Ks3ClientException("to instantiate " + clazz
					+ " has occured an exception:(" + e + ")", e);
		} catch (IllegalAccessException e) {
			// 正常情况不会抛出
			throw new Ks3ClientException("to instantiate " + clazz
					+ " has occured an exception:(" + e + ")", e);
		}
		ksResponse.setHttpRequest(req);
		ksResponse.setHttpResponse(response);
		Y ret =ksResponse.handleResponse();
		return ret;
	}
	public  <X extends Ks3WebServiceResponse<Y>, Y> Y handle(HttpUriRequest req,Class<X> clazz) throws ClientProtocolException, IOException{
		return handle(req,clazz,0);
	}
}
