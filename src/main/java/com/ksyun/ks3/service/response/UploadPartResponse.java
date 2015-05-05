package com.ksyun.ks3.service.response;

import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.http.HttpHeaders;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 上午11:27:48
 * 
 * @description 
 **/
public class UploadPartResponse extends Ks3WebServiceDefaultResponse<PartETag>{

	public int[] expectedStatus() {
		return new int[]{200};
	}

	@Override
	public void preHandle() {
		result = new PartETag();
		result.seteTag(this.getHeader(HttpHeaders.ETag.toString()));
		result.setSseAlgorithm(super.getHeader(HttpHeaders.XKssServerSideEncryption.toString()));
		result.setSseCustomerAlgorithm(super.getHeader(HttpHeaders.XKssServerSideEncryptionCustomerAlgorithm.toString()));
		result.setSseCustomerKeyMD5(super.getHeader(HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5.toString()));
		result.setSseKMSKeyId(super.getHeader(HttpHeaders.XKssServerSideEncryptionKMSKeyId.toString()));
	}
}
