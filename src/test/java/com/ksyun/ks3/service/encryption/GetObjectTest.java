package com.ksyun.ks3.service.encryption;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.ksyun.ks3.dto.GetObjectResult;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月27日 下午2:15:48
 * 
 * @description 
 **/
public class GetObjectTest extends EncryptionClientTest{
	@Test
	public void test() throws IOException{
	//	super.eo_meta.putObject(super.bucket, "php/test.txt", new ByteArrayInputStream("1234".getBytes()), null);
		GetObjectResult ret = super.eo_meta.getObject(super.bucket, "php/test.txt");
		super.writeToFile(ret.getObject().getObjectContent(), new File("D://test.txt"));
	}
}
