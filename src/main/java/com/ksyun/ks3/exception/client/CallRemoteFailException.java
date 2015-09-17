package com.ksyun.ks3.exception.client;

import com.ksyun.ks3.exception.Ks3ClientException;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年12月3日 下午2:29:12
 * 
 * @description 客户端请求KS3时出现了异常
 **/
public class CallRemoteFailException extends Ks3ClientException{

	public CallRemoteFailException(Throwable t) {
		super(t);
		// TODO Auto-generated constructor stub
	}

}
