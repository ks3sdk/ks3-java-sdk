package com.ksyun.ks3.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年11月6日 下午4:00:21
 * 
 * @description 
 **/
public class Converter {
	private static Log log = LogFactory.getLog(Converter.class);
	public static String MD52ETag(String md5)
	{
		String etag = String.format("\"%s\"", Hex.encodeHexString(Base64.decodeBase64(md5)));
		log.debug("md5 :"+md5+",convert to etag is :"+etag);
		return etag;
	}
	
	//将eTag转换成md5
	public static String ETag2MD5(String eTag)
	{
		String md5 = null;
		if(eTag.length()>=2){
			if(eTag.charAt(0)=='"'){
				eTag = eTag.substring(1, eTag.length()-1);
			}
			try {
				md5 = new String(Base64.encodeBase64(Hex.decodeHex(eTag.toCharArray())),"UTF-8");
			} catch(Exception e) {
				log.debug("Something Wrong when converter eTag to md5 :" + eTag);
			}
		}
		log.debug("etag :"+eTag+",convert to md5 is :"+md5);
		return md5;
	}
	public static void main(String[] args){
		System.out.println(Converter.ETag2MD5("81dc9bdb52d04dc20036dbd8313ed055"));
	}
}
