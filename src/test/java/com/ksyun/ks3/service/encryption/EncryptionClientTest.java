package com.ksyun.ks3.service.encryption;

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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;

import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.Ks3ClientTest;
import com.ksyun.ks3.service.encryption.model.CryptoConfiguration;
import com.ksyun.ks3.service.encryption.model.CryptoMode;
import com.ksyun.ks3.service.encryption.model.CryptoStorageMode;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterials;
import com.ksyun.ks3.service.request.GetObjectRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月9日 下午2:20:50
 * 
 * @description 
 **/
public class EncryptionClientTest extends Ks3ClientTest{
	protected Ks3EncryptionClient eo_file;
	protected Ks3EncryptionClient eo_meta;
	protected Ks3EncryptionClient ae_file;
	protected Ks3EncryptionClient ae_meta;
	protected Ks3EncryptionClient sae_file;
	protected Ks3EncryptionClient sae_meta;
	protected String bucket = "test-encryption";
	@Before
	public void initEncryption() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException{
	//	client1.clearBucket(bucket);
/*		KeyGenerator symKeyGenerator = KeyGenerator.getInstance("AES");
	    symKeyGenerator.init(256); 
	    SecretKey symKey = symKeyGenerator.generateKey();
	    EncryptionMaterials keyMaterials = new EncryptionMaterials(symKey);*/
/*		SecureRandom srand = new SecureRandom();
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(1024, srand);
        KeyPair pair = keyGenerator.generateKeyPair();
        EncryptionMaterials keyMaterials = new EncryptionMaterials(pair);*/
		SecretKey symKey = this.loadSymmetricAESKey("D://", "AES");
		 EncryptionMaterials keyMaterials = new EncryptionMaterials(symKey);
	    
		CryptoConfiguration eo_file_config = new CryptoConfiguration();
		eo_file_config.setCryptoMode(CryptoMode.EncryptionOnly);
		eo_file_config.setStorageMode(CryptoStorageMode.InstructionFile);
		this.eo_file = new Ks3EncryptionClient(super.auth1.getAccessKeyId(),super.auth1.getAccessKeySecret(),keyMaterials,eo_file_config);
		
		CryptoConfiguration eo_meta_config = new CryptoConfiguration();
		eo_meta_config.setCryptoMode(CryptoMode.EncryptionOnly);
		eo_meta_config.setStorageMode(CryptoStorageMode.ObjectMetadata);
		this.eo_meta = new Ks3EncryptionClient(super.auth1.getAccessKeyId(),super.auth1.getAccessKeySecret(),keyMaterials,eo_meta_config);
		
		CryptoConfiguration ae_file_config = new CryptoConfiguration();
		ae_file_config.setCryptoMode(CryptoMode.AuthenticatedEncryption);
		ae_file_config.setStorageMode(CryptoStorageMode.InstructionFile);
		this.ae_file = new Ks3EncryptionClient(super.auth1.getAccessKeyId(),super.auth1.getAccessKeySecret(),keyMaterials,ae_file_config);
		
		CryptoConfiguration ae_meta_config = new CryptoConfiguration();
		ae_meta_config.setCryptoMode(CryptoMode.AuthenticatedEncryption);
		ae_meta_config.setStorageMode(CryptoStorageMode.ObjectMetadata);
		this.ae_meta = new Ks3EncryptionClient(super.auth1.getAccessKeyId(),super.auth1.getAccessKeySecret(),keyMaterials,ae_meta_config);
		
		CryptoConfiguration sae_file_config = new CryptoConfiguration();
		sae_file_config.setCryptoMode(CryptoMode.StrictAuthenticatedEncryption);
		sae_file_config.setStorageMode(CryptoStorageMode.InstructionFile);
		this.sae_file = new Ks3EncryptionClient(super.auth1.getAccessKeyId(),super.auth1.getAccessKeySecret(),keyMaterials,sae_file_config);
		
		CryptoConfiguration sae_meta_config = new CryptoConfiguration();
		sae_meta_config.setCryptoMode(CryptoMode.StrictAuthenticatedEncryption);
		sae_meta_config.setStorageMode(CryptoStorageMode.ObjectMetadata);
		this.sae_meta = new Ks3EncryptionClient(super.auth1.getAccessKeyId(),super.auth1.getAccessKeySecret(),keyMaterials,sae_meta_config);
	}
	public static SecretKey loadSymmetricAESKey(String path, String algorithm)
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException {
		// Read private key from file.
		File keyFile = new File(path + "/secret.key");
		FileInputStream keyfis = new FileInputStream(keyFile);
		byte[] encodedPrivateKey = new byte[(int) keyFile.length()];
		keyfis.read(encodedPrivateKey);
		keyfis.close();

		// Generate secret key.
		return new SecretKeySpec(encodedPrivateKey, "AES");
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
	protected void rangeGetToFile(Ks3 client,String bucket,String key,String file) throws IOException{
		long maxLength = 1024*1024*5;
		long minLength = 1;
		long minPart = 4;
		long length = client.headObject(bucket, key).getObjectMetadata().getContentLength();
		if(length < maxLength*minPart)
			maxLength = length / minPart;
		if(maxLength < minLength)
			maxLength = minLength+1;
		List<String> keys = new ArrayList<String>();
		for(long current = 0l;current <= length;){
			long block = (long)(Math.random()*(minLength-maxLength+1))+maxLength;
			GetObjectResult result = rangeGet(client,bucket,key,current,current+block);
			String filename = file+"_"+current+"-"+(current+block);
			writeToFile(result.getObject().getObjectContent(),new File(filename));
			keys.add(filename);
			current+=(block+1);
		}
		mergeFiles(file,keys.toArray());
		
	}
	protected void rangeGetToFileWithThreads(final Ks3 client,final String bucket,final String key,final String file) throws IOException{
		long maxLength = 1024*1024*5;
		long minLength = 1;
		int minPart = 4;
		long length = client.headObject(bucket, key).getObjectMetadata().getContentLength();
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
						GetObjectResult result = rangeGet(client,bucket,key,cuIndex,cuIndex+curBlock-1);
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
	private GetObjectResult rangeGet(Ks3 client,String bucket,String key,long begin,long end){
		GetObjectRequest request = new GetObjectRequest(bucket,key);
		request.setRange(begin,end);
		return client.getObject(request);
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
}
