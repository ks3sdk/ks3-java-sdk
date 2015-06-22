package com.ksyun.ks3.unit;

import java.io.ByteArrayInputStream;

import org.apache.http.HttpHeaders;
import org.junit.Test;

import com.ksyun.ks3.exception.serviceside.SignatureDoesNotMatchException;
import com.ksyun.ks3.service.request.PutObjectRequest;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年6月2日 下午3:43:34
 * 
 * @description 
 **/
public class WhiteListCheck extends BaseTest{
	@Test(expected=SignatureDoesNotMatchException.class)
	public void check(){
		PutObjectRequest req = new PutObjectRequest(bucket,key,new ByteArrayInputStream("1234".getBytes()),null);
		req.getRequestConfig().getExtendHeaders().put(HttpHeaders.AUTHORIZATION.toString(),"KSS lMQTr0hNlMpB0iOk/i+x:8M9oqy8w7q6BVuPSKwfSGYsb9bg=");
		client.putObject(req);
	}
}
