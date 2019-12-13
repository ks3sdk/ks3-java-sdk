package com.ksyun.ks3.service.response;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ksyun.ks3.dto.CreateBucketConfiguration;
import com.ksyun.ks3.dto.CreateBucketConfiguration.REGION;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月16日 下午6:45:23
 * 
 * @description 获取 bucket的存储地点
 **/
public class GetBucketLocationResponse extends Ks3WebServiceXmlResponse<REGION>{

	public int[] expectedStatus() {
		return new int[]{200};
	}

	@Override
	public void preHandle() {	
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
		if("LocationConstraint".equalsIgnoreCase(getTag()))
			result = REGION.load(s);
	}

}
