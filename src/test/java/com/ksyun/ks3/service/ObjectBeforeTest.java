package com.ksyun.ks3.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.ksyun.ks3.config.ClientConfig;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.http.Ks3CoreController;

public class ObjectBeforeTest {
	protected static Ks3 client;
	protected static Ks3 clientOther;
	protected static String bucket;
	protected static String bucketOther;
	protected static String filePath;
	private static Log logger = LogFactory.getLog(ObjectBeforeTest.class);
	
	/**
	 * @throws IOException 
	 * @description 加载类时调用，配置测试客户端数据，权限数据，和基础数据信息。
	 * 		
	 */
	@BeforeClass
	public static void beforeClass() throws IOException{
		ClientConfig.getConfig().set(ClientConfig.CLIENT_URLFORMAT, "1");
		
		Properties credential = new Properties();
		String accesskeyId1 = null;
		String accesskeySecret1 = null;
		String accesskeyId2 = null;
		String accesskeySecret2 = null;
		try {
			credential.load(ObjectTest.class.getResourceAsStream("/accesskey.properties"));// resourece 路径存在问题 NullPointerException
			accesskeyId1 = credential.getProperty("accesskeyid1");
			accesskeySecret1 = credential.getProperty("accesskeysecret1");
			accesskeyId2 = credential.getProperty("accesskeyid2");
			accesskeySecret2 = credential.getProperty("accesskeysecret2");
			
		} catch (Exception e) {
			logger.warn("Error massage : " + e.toString());
			accesskeyId1 = "";
			accesskeySecret1 = "";
			accesskeyId2 = "";
			accesskeySecret2 = "";
			
		}

		client = new Ks3Client(accesskeyId1,accesskeySecret1);
		
		clientOther = new Ks3Client(accesskeyId2,accesskeySecret2);
		
		new Ks3CoreController();
		
		
		//测试时需进行的配置：
		bucket = "test1-zzy-jr";
		bucketOther = "test2-zzy-jr";
		
		//若第一次运行测试用例需要将initFile配置为 true，运行后改为false，避免重复上传影响效率
		Boolean initFile = true;
		
		filePath = "D:/objectTest";
		File fileDir = new File(filePath);
		fileDir.mkdir();
		
		if(initFile){
			init();
		}
		
	}
	
	private static void init() throws IOException{
		
		
		//add test files
		String[] fileNames = {
			"deleteTestP.txt","putObjectTest.txt","putObjectTestP.txt",
			"putObjectHeaders.txt","headObjectHeaders.txt","abc.txt","record.txt","putObjectHeaders.txt","getObjectHeaders.txt","hosts.txt"
			,"hostsPulbic.txt","putObjectTestMeta.txt","putObjectTestP1.txt"
		};
		
		for(String fileName:fileNames){
			File file = new File(filePath+ "/" + fileName);
			System.out.println(file.getPath());
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file);
			fw.write("test file:" + file.getPath());
			
			fw.close();
		}
		
		//初始化文件
		client.putObject(bucket, "deleteTestP.txt", new File(filePath + "/getObjectHeaders.txt"));
		client.putObject(bucket, "putObjectTest.txt", new File(filePath + "/putObjectTest.txt"));
		client.putObject(bucket, "putObjectTestP.txt", new File(filePath + "/putObjectTestP.txt"));
		client.putObject(bucket, "putObjectHeaders.txt", new File(filePath + "/putObjectHeaders.txt"));
		client.putObject(bucket, "headObjectHeaders.txt", new File(filePath + "/headObjectHeaders.txt"));
		client.putObject(bucket, "abc.txt", new File(filePath + "/abc.txt"));
		client.putObject(bucket, "record.txt", new File(filePath + "/record.txt"));
		client.putObject(bucket, "putObjectHeaders.txt", new File(filePath + "/putObjectHeaders.txt"));
		client.putObject(bucket, "getObjectHeaders.txt", new File(filePath + "/getObjectHeaders.txt"));
		client.putObject(bucket, "hosts.txt", new File(filePath + "/hosts.txt"));
		client.putObject(bucket, "hostsPulbic.txt", new File(filePath + "/hostsPulbic.txt"));
		client.putObject(bucket, "putObjectTestMeta.txt", new File(filePath + "/putObjectTestMeta.txt"));
		client.putObject(bucket, "putObjectTestP1.txt", new File(filePath + "/putObjectTestP1.txt"));
	}

	/**
	 * @description 配置测试客户端数据，执行每一个测试用例前都会执行 @Before 中代码
	 */
	@AfterClass
	public static void afterClass(){
		
	}
	
	public List<String> getETags(){
		List<String> eTags = new ArrayList<String>();
		try{
			GetObjectResult result = client.getObject(bucket, "hosts.txt");
			eTags.add(result.getObject().getObjectMetadata().getETag());
			
			result = client.getObject(bucket, "putObjectTest.txt");
			eTags.add(result.getObject().getObjectMetadata().getETag());
			
			result = client.getObject(bucket, "putObjectTestP.txt");
			eTags.add(result.getObject().getObjectMetadata().getETag());
			
			result = client.getObject(bucket, "hostsPulbic.txt");
			eTags.add(result.getObject().getObjectMetadata().getETag());
			
			result = client.getObject(bucket, "getObjectHeaders.txt");
			eTags.add(result.getObject().getObjectMetadata().getETag());
			
			result = client.getObject(bucket, "headObjectHeaders.txt");
			eTags.add(result.getObject().getObjectMetadata().getETag());
		}catch(Exception e){
			e.printStackTrace();
		}
		return eTags;
	}
	
}
