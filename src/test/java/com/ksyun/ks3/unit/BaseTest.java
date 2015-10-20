package com.ksyun.ks3.unit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.http.HttpClientFactory;
import com.ksyun.ks3.service.Ks3Client;
import com.ksyun.ks3.service.Ks3ClientConfig;
import com.ksyun.ks3.service.encryption.Ks3EncryptionClient;
import com.ksyun.ks3.service.encryption.model.CryptoConfiguration;
import com.ksyun.ks3.service.encryption.model.CryptoMode;
import com.ksyun.ks3.service.encryption.model.CryptoStorageMode;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterials;

public class BaseTest {
	protected static Ks3Client client;
	protected static Ks3Client usClient;
	protected static SecretKey symKey;
	protected static Ks3EncryptionClient eo_file;
	protected static Ks3EncryptionClient eo_meta;
	protected static Ks3EncryptionClient ae_file;
	protected static Ks3EncryptionClient ae_meta;
	protected static Ks3EncryptionClient sae_file;
	protected static Ks3EncryptionClient sae_meta;
	protected static HttpClient hclient;
	protected static String bucket = "ks3kssjavasdk2";
	protected static String key = "/test.中//\\文?？.key//";
	protected static String key_copy = key+".copy";
	protected static String key_instruction = key+".instruction";
	protected static String dir;
	protected static String filename = "file";
	protected static String filedown = "file_down";
	@BeforeClass
	public static void init() throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException{
		
		final Properties credential = new Properties();
		credential.load(BaseTest.class.getClass()
				.getResourceAsStream("/accesskey.properties"));
		
		URL url = BaseTest.class.getClass().getResource("/accesskey.properties");
		dir = url.getPath().toString();
		dir = dir.substring(0,dir.length() - "/accesskey.properties".length()+1);

		final String accesskeyId = credential.getProperty("accesskeyid");
		final String accesskeySecret = credential.getProperty("accesskeysecret");
		client = new Ks3Client(accesskeyId,accesskeySecret);
		usClient = new Ks3Client(accesskeyId,accesskeySecret);
		usClient.setEndpoint("ks3-us-west-1.ksyun.com");
		
		symKey = loadSymmetricAESKey();
		EncryptionMaterials keyMaterials = new EncryptionMaterials(symKey);
	    
		CryptoConfiguration eo_file_config = new CryptoConfiguration();
		eo_file_config.setCryptoMode(CryptoMode.EncryptionOnly);
		eo_file_config.setStorageMode(CryptoStorageMode.InstructionFile);
		eo_file = new Ks3EncryptionClient(accesskeyId,accesskeySecret,keyMaterials,eo_file_config);
		
		CryptoConfiguration eo_meta_config = new CryptoConfiguration();
		eo_meta_config.setCryptoMode(CryptoMode.EncryptionOnly);
		eo_meta_config.setStorageMode(CryptoStorageMode.ObjectMetadata);
		eo_meta = new Ks3EncryptionClient(accesskeyId,accesskeySecret,keyMaterials,eo_meta_config);
		
		CryptoConfiguration ae_file_config = new CryptoConfiguration();
		ae_file_config.setCryptoMode(CryptoMode.AuthenticatedEncryption);
		ae_file_config.setStorageMode(CryptoStorageMode.InstructionFile);
		ae_file = new Ks3EncryptionClient(accesskeyId,accesskeySecret,keyMaterials,ae_file_config);
		
		CryptoConfiguration ae_meta_config = new CryptoConfiguration();
		ae_meta_config.setCryptoMode(CryptoMode.AuthenticatedEncryption);
		ae_meta_config.setStorageMode(CryptoStorageMode.ObjectMetadata);
		ae_meta = new Ks3EncryptionClient(accesskeyId,accesskeySecret,keyMaterials,ae_meta_config);
		
		CryptoConfiguration sae_file_config = new CryptoConfiguration();
		sae_file_config.setCryptoMode(CryptoMode.StrictAuthenticatedEncryption);
		sae_file_config.setStorageMode(CryptoStorageMode.InstructionFile);
		sae_file = new Ks3EncryptionClient(accesskeyId,accesskeySecret,keyMaterials,sae_file_config);
		
		CryptoConfiguration sae_meta_config = new CryptoConfiguration();
		sae_meta_config.setCryptoMode(CryptoMode.StrictAuthenticatedEncryption);
		sae_meta_config.setStorageMode(CryptoStorageMode.ObjectMetadata);
		sae_meta = new Ks3EncryptionClient(accesskeyId,accesskeySecret,keyMaterials,sae_meta_config);
		
		hclient = new HttpClientFactory().createHttpClient(new Ks3ClientConfig().getHttpClientConfig());
	}
	@Before
	public void before() throws InterruptedException{
		if(client.bucketExists(bucket)){
			client.clearBucket(bucket);
			client.putBucketACL(bucket, CannedAccessControlList.Private);
			client.putBucketLogging(bucket, false, null);
			Thread.sleep(3000);
		}else{
			client.createBucket(bucket);
		}
	}
	@After
	public void after(){
		new File(dir+filename).delete();
		new File(dir+filedown).delete();
	}
	public static SecretKey loadSymmetricAESKey()
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException {
		// Read private key from file.
		InputStream keyfis = BaseTest.class.getClass().getResourceAsStream("/secret.key");
		byte[] encodedPrivateKey = new byte[32];
		keyfis.read(encodedPrivateKey);
		keyfis.close();

		// Generate secret key.
		return new SecretKeySpec(encodedPrivateKey, "AES");
	}
}
