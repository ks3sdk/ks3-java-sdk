package com.ksyun.ks3.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.encryption.Ks3EncryptionClient;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.transfer.Ks3UploadClient;
import com.ksyun.ks3.utils.Md5Utils;

public class Ks3EncryptionClientTest extends BaseTest{
	@Before
	public void initEncrypFile(){
		TestUtils.makeFile(dir+filename,1024*200);
	}
	@Test
	public void testPutEO_Meta() throws IOException{
		checkSimplePutAndGet(eo_meta);
	}
	@Test
	public void testPutEO_File() throws IOException{
		checkSimplePutAndGet(eo_file);
	}
	@Test
	public void testPutAE_Meta() throws IOException{
		checkSimplePutAndGet(ae_meta);
	}
	@Test
	public void testPutAE_File() throws IOException{
		checkSimplePutAndGet(ae_file);
	}
	@Test
	public void testPutSAE_Meta() throws IOException{
		checkSimplePutAndGet(sae_meta);
	}
	@Test
	public void testPutSAE_File() throws IOException{
		checkSimplePutAndGet(sae_file);
	}
	
	
	private void checkSimplePutAndGet(Ks3EncryptionClient client) throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		client.putObject(req);	
		
		GetObjectResult result = client.getObject(bucket, key);
		TestUtils.writeToFile(result.getObject().getObjectContent(), new File(dir+filedown));
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
	}
	@Test
	public void testPutEO_File_Stream(){
		ObjectMetadata meta = new ObjectMetadata();
		eo_file.putObject(bucket, key, new ByteArrayInputStream("1234".getBytes()),meta);
	}
	@Test
	public void testPutAE_File_Stream(){
		ObjectMetadata meta = new ObjectMetadata();
		ae_file.putObject(bucket, key, new ByteArrayInputStream("1234".getBytes()),meta);
	}
	@Test
	public void testPutEO_Meta_Range() throws IOException{
		checkSimplePutAndGetRange(eo_meta);
	}
	@Test
	public void testPutEO_File_Range() throws IOException{
		checkSimplePutAndGetRange(eo_file);
	}
	@Test
	public void testPutAE_Meta_Range() throws IOException{
		checkSimplePutAndGetRange(ae_meta);
	}
	@Test
	public void testPutAE_File_Range() throws IOException{
		checkSimplePutAndGetRange(ae_file);
	}
	@Test(expected=SecurityException.class)
	public void testPutSAE_Meta_Range() throws IOException{
		checkSimplePutAndGetRange(sae_meta);
	}
	@Test(expected=SecurityException.class)
	public void testPutSAE_File_Range() throws IOException{
		checkSimplePutAndGetRange(sae_file);
	}
	private void checkSimplePutAndGetRange(Ks3EncryptionClient client) throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		client.putObject(req);	
		TestUtils.rangeGetToFile(client, bucket, key, dir+filedown);
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
	}
	
	@Test
	public void testPutEO_Meta_Range_Threads() throws IOException{
		checkSimplePutAndGetRangeThreads(eo_meta);
	}
	@Test
	public void testPutEO_File_Range_Threads() throws IOException{
		checkSimplePutAndGetRangeThreads(eo_file);
	}
	@Test
	public void testPutAE_Meta_Range_Threads() throws IOException{
		checkSimplePutAndGetRangeThreads(ae_meta);
	}
	@Test
	public void testPutAE_File_Range_Threads() throws IOException{
		checkSimplePutAndGetRangeThreads(ae_file);
	}
	@Test(expected=SecurityException.class)
	public void testPutSAE_Meta_Range_Threads() throws IOException{
		checkSimplePutAndGetRangeThreads(sae_meta);
	}
	@Test(expected=SecurityException.class)
	public void testPutSAE_File_Range_Threads() throws IOException{
		checkSimplePutAndGetRangeThreads(sae_file);
	}
	private void checkSimplePutAndGetRangeThreads(Ks3 client) throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		client.putObject(req);	
		TestUtils.rangeGetToFileWithThreads(client, bucket, key, dir+filedown);
		assertEquals(new File(dir+filename).length(),new File(dir+filedown).length());
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
	}
	
	
	@Test
	public void testDeleteObject_File(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		ae_file.putObject(req);	
		assertTrue(ae_file.objectExists(bucket,key));
		assertTrue(ae_file.objectExists(bucket, key_instruction));
		ae_file.deleteObject(bucket, key);
		assertFalse(ae_file.objectExists(bucket, key));
		assertFalse(ae_file.objectExists(bucket, key_instruction));
	}	
	@Test
	public void testDeleteObject_Meta(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		ae_meta.putObject(req);	
		assertTrue(ae_meta.objectExists(bucket, key));
		assertFalse(ae_meta.objectExists(bucket, key_instruction));
		ae_meta.deleteObject(bucket, key);
		assertFalse(ae_meta.objectExists(bucket, key));
		assertFalse(ae_meta.objectExists(bucket,key_instruction));
	}
	@Test
	public void testMultiEO_Meta() throws IOException{
		checkMultipartUpload(eo_meta);
	}
	@Test
	public void testMultiEO_File() throws IOException{
		checkMultipartUpload(eo_file);
	}
	@Test
	public void testMultiAE_Meta() throws IOException{
		checkMultipartUpload(ae_meta);
	}
	@Test
	public void testMultiAE_File() throws IOException{
		checkMultipartUpload(ae_file);
	}
	@Test
	public void testMultiSAE_Meta() throws IOException{
		checkMultipartUpload(sae_meta);
	}
	@Test
	public void testMultiSAE_File() throws IOException{
		checkMultipartUpload(sae_file);
	}
	private void checkMultipartUpload(Ks3EncryptionClient client) throws IOException{
		Ks3UploadClient upClient = new Ks3UploadClient(client);
		upClient.uploadFile(bucket, key,new File(dir+filename));
		
		GetObjectResult result = client.getObject(bucket, key);
		TestUtils.writeToFile(result.getObject().getObjectContent(), new File(dir+filedown));
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
		
	}
	@Test
	public void testMultiEO_Meta_Range() throws IOException{
		checkMultipartUploadGetRange(eo_meta);
	}
	@Test
	public void testMultiEO_File_Range() throws IOException{
		checkMultipartUploadGetRange(eo_file);
	}
	@Test
	public void testMultiAE_Meta_Range() throws IOException{
		checkMultipartUploadGetRange(ae_meta);
	}
	@Test
	public void testMultiAE_File_Range() throws IOException{
		checkMultipartUploadGetRange(ae_file);
	}
	@Test(expected=SecurityException.class)
	public void testMultiSAE_Meta_Range() throws IOException{
		checkMultipartUploadGetRange(sae_meta);
	}
	@Test(expected=SecurityException.class)
	public void testMultiSAE_File_Range() throws IOException{
		checkMultipartUploadGetRange(sae_file);
	}
	private void checkMultipartUploadGetRange(Ks3EncryptionClient client) throws IOException{
		Ks3UploadClient upClient = new Ks3UploadClient(client);
		upClient.uploadFile(bucket, key,new File(dir+filename));
		
		TestUtils.rangeGetToFile(client, bucket, key, dir+filedown);
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
	}
	
	@Test
	public void testMultiEO_Meta_Range_Threads() throws IOException{
		checkMultipartUploadGetRangeThreads(eo_meta);
	}
	@Test
	public void testMultiEO_File_Range_Threads() throws IOException{
		checkMultipartUploadGetRangeThreads(eo_file);
	}
	@Test
	public void testMultiAE_Meta_Range_Threads() throws IOException{
		checkMultipartUploadGetRangeThreads(ae_meta);
	}
	@Test
	public void testMultiAE_File_Range_Threads() throws IOException{
		checkMultipartUploadGetRangeThreads(ae_file);
	}
	@Test(expected=SecurityException.class)
	public void testMultiSAE_Meta_Range_Threads() throws IOException{
		checkMultipartUploadGetRangeThreads(sae_meta);
	}
	@Test(expected=SecurityException.class)
	public void testMultiSAE_File_Range_Threads() throws IOException{
		checkMultipartUploadGetRangeThreads(sae_file);
	}
	private void checkMultipartUploadGetRangeThreads(Ks3EncryptionClient client) throws IOException{
		Ks3UploadClient upClient = new Ks3UploadClient(client);
		upClient.uploadFile(bucket, key,new File(dir+filename));
		
		TestUtils.rangeGetToFileWithThreads(client, bucket, key, dir+filedown);
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
	}
	@Test
	public void testPutEO_Meta_Part() throws IOException{
		partUploadAndCheck(eo_meta);
	}
	private void partUploadAndCheck(Ks3 client) throws IOException{
		PutObjectRequest req = new PutObjectRequest(bucket,key,new File(dir+filename));
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(10);
		req.setObjectMeta(meta);
		client.putObject(req);	
		
		GetObjectResult result = client.getObject(bucket, key);
		TestUtils.writeToFile(result.getObject().getObjectContent(), new File(dir+filedown));
		assertEquals(Md5Utils.md5AsBase64(new File(dir+filename)),
				Md5Utils.md5AsBase64(new File(dir+filedown)));
	}
}
