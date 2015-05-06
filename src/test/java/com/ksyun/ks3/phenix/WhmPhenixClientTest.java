package com.ksyun.ks3.phenix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author whm[wanghaiming1@kingsoft.com]  
 * 
 * @date 2015年04月20日 上午10:28:53
 * 
 * @description
 **/
public class WhmPhenixClientTest extends PhenixClientTest {

	@Test
	public void test() throws Ks3ServiceException, Ks3ClientException, IOException {
//		client.createBucket("murongsyifei");//只创建一次,相当于/home/murongyifei这样的概念,   如果有拒绝创建,409 Conflict代表已经存在

		client.putObject("murongyifei", "test1", new FileInputStream(new File("/Users/whm/Desktop/test")),null);
		System.out.println("oldMd5:"+Md5Utils.md5AsBase64(new File("/Users/whm/Desktop/test")));
		
		client.putObject("murongyifei", "test2", new FileInputStream(new File("/Users/whm/Desktop/test")),null);
		System.out.println("oldMd5:"+Md5Utils.md5AsBase64(new File("/Users/whm/ksyun/ks3-kss-java-sdk/src/test/resources/1234.jpeg")));
		
		client.putObject("murongyifei", "test3", new FileInputStream(new File("/Users/whm/Desktop/test")),null);
		System.out.println("oldMd5:"+Md5Utils.md5AsBase64(new File("/Users/whm/ksyun/ks3-kss-java-sdk/src/test/resources/IMG.jpg")));
		
//		GetObjectResult object = client.getObject("murongyifei", "test1");
//		String newMd5 = Md5Utils.md5AsBase64(object.getObject().getObjectContent());
//		System.out.println("newMd5:"+newMd5);
	}
	
//	@Test
	public void createBucket() {
		String bucketName = "murongyifei.0";
		boolean b = client.bucketExists(bucketName);
		if (!b) {
			System.out.println(client.createBucket(bucketName));
		}
	}
	
//	@Test
	public void get() throws IOException {
		String bucketName = "murongyifei1.0";
		//murongyifei1.0_1634m
		String key = "murongyifei1.0_1634m";
		
		GetObjectResult object = client.getObject(bucketName, key);
		
		String filePath = "/Users/whm/Desktop";
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
}
