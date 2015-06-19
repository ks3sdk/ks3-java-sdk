package com.ksyun.ks3.signer;

import org.apache.http.HttpHeaders;

import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;
import com.ksyun.ks3.utils.AuthUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月6日 上午10:45:56
 * 
 * @description 默认的签名计算器
 **/
public class DefaultSigner implements Signer {
	public void sign(Authorization auth, Request request) {
		try {
			if(!request.isPresign())
				request.addHeader(HttpHeaders.AUTHORIZATION.toString(),AuthUtils.calcAuthorization(auth, request));
			else{
				request.getQueryParams().put("AccessKeyId",auth.getAccessKeyId());
				request.getQueryParams().put("Signature",AuthUtils.calcSignature(auth.getAccessKeySecret(), request));
				request.getQueryParams().put("Expires", String.valueOf(request.getExpires().getTime()/1000));
			}
		} catch (Exception e) {
			throw new Ks3ClientException(
					"计算用户签名时出错("
							+ e + ")", e);
		}
	}

}
