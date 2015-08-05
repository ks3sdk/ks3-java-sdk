package com.ksyun.ks3.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.Authorization;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;
import com.ksyun.ks3.utils.DateUtils.DATETIME_PROTOCOL;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月16日 下午7:31:45
 * 
 * @description 
 **/
public class AuthUtils {
	private static final Log log = LogFactory.getLog(AuthUtils.class); 
	public static String calcAuthorization (Authorization auth,Request request) throws SignatureException
	{
		String signature = calcSignature(auth.getAccessKeySecret(),request);
		String value = Constants.authHeaderPrefix+" "+auth.getAccessKeyId()+":"+signature;
		return value;
	}
	//post表单时的签名
	/**
	 * 
	 * @param accessKeySecret
	 * @param policy  getPolicy(Date expiration,String bucket)得到的结果
	 * @return
	 * @throws SignatureException
	 */
	public static String calcSignature(String accessKeySecret,String policy) throws SignatureException{
		String signStr = policy;
		log.debug("StringToSign:"+signStr);
		return calculateRFC2104HMAC(signStr,accessKeySecret);
	}
	//post表单时的policy
	/**
	 * 
	 * @param expiration 该签名过期时间
	 * @param bucket 该签名只能在该bucket上使用
	 * @return
	 */
	@Deprecated
	public static String getPolicy(Date expiration,String bucket) {
		String policy = "{\"expiration\": \""
						+DateUtils.convertDate2Str(expiration, DATETIME_PROTOCOL.ISO8861)
						+"\",\"conditions\": [ {\"bucket\": \""+bucket+"\"}]}";
		log.debug("policy:"+policy);
		try {
			String _policy = new String(Base64.encodeBase64(policy.getBytes("UTF-8")),"UTF-8");
			return _policy;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	//外链
	public static String calcSignature(String accessKeySecret,String bucket,String key,Map<String,String> params,String requestMethod,long _signDate) throws SignatureException
	{
		String paramsToSign = encodeParams(params);
		String resource = "/"+bucket+"/"+key;
		resource = resource.replace("//", "/%2F");
		if(!StringUtils.isBlank(paramsToSign))
			resource+="?"+paramsToSign;
		 List<String> signList = new ArrayList<String>();
	        signList.addAll(Arrays.asList(new String[] {
	                requestMethod,"","",String.valueOf(_signDate),resource
	        }));
	    String signStr = StringUtils.join(signList.toArray(), "\n");
	    log.debug("StringToSign:"+signStr.replace("\n","\\n"));
		return calculateRFC2104HMAC(signStr, accessKeySecret);
	}
	//普通
	public static String calcSignature (String accessKeySecret,Request request) throws SignatureException
	{
        String resource = CanonicalizedKSSResource(request);
        String requestMethod = request.getMethod().toString();
        String contentMd5 = request.getHeaders().containsKey(HttpHeaders.ContentMD5.toString())?request.getHeaders().get(HttpHeaders.ContentMD5.toString()):"";
        String contentType = request.getHeaders().containsKey(HttpHeaders.ContentType.toString())?request.getHeaders().get(HttpHeaders.ContentType.toString()):"";
        String _signDate;
        if(request.isPresign()){
        	_signDate =String.valueOf(request.getExpires().getTime()/1000);
        }else{
            request.addHeaderIfNotContains(HttpHeaders.Date.toString(), DateUtils.convertDate2Str(new Date(), DATETIME_PROTOCOL.RFC1123));
            _signDate = request.getHeaders().get(HttpHeaders.Date.toString());	
        }

        List<String> signList = new ArrayList<String>();
        signList.addAll(Arrays.asList(new String[] {
                requestMethod, contentMd5, contentType, _signDate
        }));

        String _headers = CanonicalizedKSSHeaders(request);
        if (_headers != null && !_headers.equals("")){
            signList.add(_headers);
        }

        signList.add(resource);
        
        String signStr = StringUtils.join(signList.toArray(), "\n");
        
        log.debug("StringToSign:"+signStr.replace("\n","\\n"));
        
        String serverSignature = calculateRFC2104HMAC(signStr, accessKeySecret);
        return serverSignature;
	}
    public static String CanonicalizedKSSResource(Request request) {
    	boolean escapeDoubleSlash = true;

        String bucketName = request.getBucket();
        String objectKey = request.getKey();

        StringBuffer buffer = new StringBuffer();
        buffer.append("/");
        if (!StringUtils.isBlank(bucketName)) {
            buffer.append(bucketName).append("/");
        }
        
        if (!StringUtils.isBlank(objectKey)) {
        	String encodedPath = HttpUtils.urlEncode(objectKey, true);
            buffer.append(encodedPath);
        }
        
        String resource = buffer.toString();
        if (escapeDoubleSlash) {
        	resource = resource.replace("//", "/%2F");
        }

        String queryParams = encodeParams(request.getQueryParams());
        if (queryParams != null && !queryParams.equals(""))
        	resource = resource + "?" + queryParams;
        return resource;
    }
    private static String CanonicalizedKSSHeaders(Request request) {
    	String prefix = Constants.specHeaderPrefix;
        Map<String, String> headers = request.getHeaders();

        List<String> headList = new ArrayList<String>();

        for (String _header : headers.keySet()) {
            if (_header.toLowerCase().startsWith(prefix)) {
                headList.add(_header);
            }
        }

        Collections.sort(headList, new Comparator<String>() {

			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
         
        });
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < headList.size(); i++) {
            String _key = headList.get(i);
            buffer.append(headList.get(i).toLowerCase() + ":" + headers.get(_key));
            if (i < (headList.size() - 1))
                buffer.append("\n");
        }
        return buffer.toString();
    }
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	public static String calculateRFC2104HMAC(String data, String key)
			throws java.security.SignatureException {
		String result;
		try {

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
					HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			result = new String(Base64.encodeBase64(rawHmac),"GBK");

		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : "
					+ e);
		}
		return result;
	}
	//专为计算resource提供的方法
	public static String encodeParams(Map<String,String> params) {
		List<Map.Entry<String, String>> arrayList = new ArrayList<Map.Entry<String, String>>(
				params.entrySet());
		Collections.sort(arrayList,
				new Comparator<Map.Entry<String, String>>() {
					public int compare(Entry<String, String> o1,
							Entry<String, String> o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				});
		List<String> kvList = new ArrayList<String>();
		for (Entry<String, String> entry : arrayList) {
			String value = null;
			//8203,直接从浏览器粘下来的字符串中可能含有这个非法字符
			String key = entry.getKey().replace(String.valueOf((char)8203),"");
			if (!StringUtils.isBlank(entry.getValue()))
				value = entry.getValue();
			if (RequestUtils.subResource.contains(entry.getKey())||RequestUtils.QueryParam.contains(entry.getKey())) {
				if (value != null && !value.equals(""))
					kvList.add(key + "=" + value);
				else{
					if (RequestUtils.subResource.contains(key))
						kvList.add(key);
				}
			}
		}

		return StringUtils.join(kvList.toArray(), "&");
	}
}
