package com.ksyun.ks3.service.response;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import com.ksyun.ks3.dto.Ks3Result;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午3:47:35
 * 
 * @description 
 **/
public class DeleteObjectResponse extends Ks3WebServiceDefaultResponse<Ks3Result>{

	public int[] expectedStatus() {
		return new int[]{204};
	}

	@Override
	public void preHandle() {
		this.result = new Ks3Result();
	}
}
