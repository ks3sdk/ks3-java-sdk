package com.ksyun.ks3.service.response;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ksyun.ks3.dto.CopyResult;
import com.ksyun.ks3.utils.DateUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月17日 下午2:29:22
 * 
 * @description 
 **/
public class CopyPartResponse extends Ks3WebServiceXmlResponse<CopyResult>{

	public int[] expectedStatus() {
		return new int[]{200};
	}

	@Override
	public void preHandle() {
		
	}

	@Override
	public void startDocument() throws SAXException {
		result = new CopyResult();
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
		if("LastModified".equals(getTag())){
			result.setLastModified(DateUtils.convertStr2Date(s));
		}else if("ETag".equals(getTag())){
			result.setETag(s.replace("\"", ""));
		}
	}
}
