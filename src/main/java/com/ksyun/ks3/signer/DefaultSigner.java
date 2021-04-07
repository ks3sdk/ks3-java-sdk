package com.ksyun.ks3.signer;

import com.ksyun.ks3.dto.*;
import com.ksyun.ks3.service.Ks3ClientConfig;
import org.apache.http.HttpHeaders;

import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.http.Request;
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
			if(!request.isPresign()) {
				if (auth.getSecurityToken() != null) {
					request.addHeader(com.ksyun.ks3.http.HttpHeaders.XKssSecurityTokenHeader.toString(), auth.getSecurityToken());
				}
				request.addHeader(HttpHeaders.AUTHORIZATION.toString(),AuthUtils.calcAuthorization(auth, request));
			} else{
				if (auth.getSecurityToken() != null) {
					request.getQueryParams().put("security-token", auth.getSecurityToken());
				}
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
	public void signV4(Authorization auth, Request request,Ks3ClientConfig ks3config) {
		try {
			Ks3V4Signer signer = new Ks3V4Signer();
			if(!request.isPresign()) {//expires == null
				if (auth.getSecurityToken() != null) {
					request.addHeader(com.ksyun.ks3.http.HttpHeaders.XKssSecurityTokenHeader.toString(), auth.getSecurityToken());
				}
				signer.sign(auth, request, ks3config);
			}else{
				if (auth.getSecurityToken() != null) {
					request.getQueryParams().put("security-token", auth.getSecurityToken());
				}
			    String signature = signer.calcSignature(auth, request,ks3config);
				request.addQueryParam("X-Kss-Signature",signature);
			}
		} catch (Exception e) {
			throw new Ks3ClientException(
					"计算用户签名时出错("
							+ e + ")", e);
		}
	}

}
