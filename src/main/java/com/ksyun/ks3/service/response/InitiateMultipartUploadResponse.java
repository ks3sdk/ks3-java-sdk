package com.ksyun.ks3.service.response;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.http.HttpHeaders;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 上午10:49:15
 * 
 * @description 
 **/
public class InitiateMultipartUploadResponse extends Ks3WebServiceXmlResponse<InitiateMultipartUploadResult>{

	public int[] expectedStatus() {
		return new int[]{200};
	}

	@Override
	public void preHandle() {
		result = new InitiateMultipartUploadResult();
		result.setSseAlgorithm(super.getHeader(HttpHeaders.XKssServerSideEncryption.toString()));
		result.setSseCustomerAlgorithm(super.getHeader(HttpHeaders.XKssServerSideEncryptionCustomerAlgorithm.toString()));
		result.setSseCustomerKeyMD5(super.getHeader(HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5.toString()));
		result.setSseKMSKeyId(super.getHeader(HttpHeaders.XKssServerSideEncryptionKMSKeyId.toString()));
	}

	@Override
	public void startDocument() throws SAXException {
		
	}

	@Override
	public void startEle(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
	}

	@Override
	public void endEle(String uri, String localName, String qName)
			throws SAXException {
		
	}

	@Override
	public void string(String s) {
		if("UploadId".equalsIgnoreCase(getTag()))
		{
			this.result.setUploadId(s);
		}
	}

}
