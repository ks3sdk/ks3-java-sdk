package com.ksyun.ks3.service;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;

import com.ksyun.ks3.config.AWSConfigLoader;
import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.http.Ks3CoreController;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月7日 下午1:46:05
 * 
 * @description
 **/
public class Ks3ClientTest {
	/**
	 * 方法是否应该抛出异常
	 * shouldThrowException
	 */
	protected boolean ste = false;
	/**
	 * 是否接受到异常
	 */
	protected boolean isc = false;
	protected Ks3 client;
	protected Ks3 client1;
	protected Ks3 client2;
	protected Ks3CoreController controller;
	protected Authorization auth;
	protected Authorization auth1;
	protected Authorization auth2;
	@Before
	public void init() throws IOException {

	//	ClientConfig.getConfig().set(ClientConfig.CLIENT_URLFORMAT, "1");
		ClientConfig.getConfig().set(ClientConfig.END_POINT, "kssws.ks-cdn.com");
		//nginx把数据刷到服务端需要时间较长
		ClientConfig.getConfig().set(ClientConfig.SOCKET_TIMEOUT,"300000");
		
		
		final Properties credential = new Properties();
		credential.load(this.getClass().getClassLoader()
				.getResourceAsStream("accesskey.properties"));

		final String accesskeyId = credential.getProperty("accesskeyid");
		final String accesskeySecret = credential.getProperty("accesskeysecret");
		this.client = new Ks3Client(accesskeyId,accesskeySecret);
		this.auth = new Authorization(accesskeyId,accesskeySecret);
		
		final String accesskeyId1 = credential.getProperty("accesskeyid1");
		final String accesskeySecret1 = credential.getProperty("accesskeysecret1");
		this.client1 = new Ks3Client(accesskeyId1,accesskeySecret1);
		this.auth1 = new Authorization(accesskeyId1,accesskeySecret1);
		
		final String accesskeyId2 = credential.getProperty("accesskeyid2");
		final String accesskeySecret2 = credential.getProperty("accesskeysecret2");
		this.client2 = new Ks3Client(accesskeyId2,accesskeySecret2);
		this.auth2 = new Authorization(accesskeyId2,accesskeySecret2);
		this.controller = new Ks3CoreController();
	}
}
