package com.ksyun.ks3.service.encryption.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;

import com.ksyun.ks3.config.AWSConfigLoader;
import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.Ks3Client;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月13日 下午4:59:27
 * 
 * @description
 **/
public class AWSEncryptionTest {
	protected Ks3 client;
	protected String bucket = "buckettestsrunner-awsputbucketwithnameandregion-24c3p0p";
	//protected String bucket = "test1-zzy-jr";
	protected SecretKey symKey;
	private static final String keyName = "secret.key";
	
	@Before
	public void init() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException {
		symKey = loadSymmetricAESKey("D://", "AES");     

		ClientConfig.addConfigLoader(new AWSConfigLoader());
		client = new Ks3Client("AKIAIN3WVZLXKDUS242Q","5iDtwjnwgFeeKxqXy8OQqs6hTOrx/4Dyk8YBBFwn");
		//client = new Ks3Client("lMQTr0hNlMpB0iOk/i+x","D4CsYLs75JcWEjbiI22zR3P7kJ/+5B1qdEje7A7I");
	}

	public static void saveSymmetricKey(String path, SecretKey secretKey)
			throws IOException {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				secretKey.getEncoded());
		FileOutputStream keyfos = new FileOutputStream(path + "/" + keyName);
		keyfos.write(x509EncodedKeySpec.getEncoded());
		keyfos.close();
	}

	public static SecretKey loadSymmetricAESKey(String path, String algorithm)
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException {
		// Read private key from file.
		File keyFile = new File(path + "/" + keyName);
		FileInputStream keyfis = new FileInputStream(keyFile);
		byte[] encodedPrivateKey = new byte[(int) keyFile.length()];
		keyfis.read(encodedPrivateKey);
		keyfis.close();

		// Generate secret key.
		return new SecretKeySpec(encodedPrivateKey, "AES");
	}
}
