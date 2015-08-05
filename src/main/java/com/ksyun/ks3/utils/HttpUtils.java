package com.ksyun.ks3.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.AccessControlPolicy;
import com.ksyun.ks3.dto.Adp;
import com.ksyun.ks3.dto.Grant;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.Permission;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.dto.SSEKssKMSParams;
import com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.utils.DateUtils.DATETIME_PROTOCOL;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月17日 上午10:23:53
 * 
 * @description
 **/
public class HttpUtils {
	private static final Log log = LogFactory.getLog(HttpUtils.class);
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final Pattern ENCODED_CHARACTERS_PATTERN;
	static {
		StringBuilder pattern = new StringBuilder();

		pattern.append(Pattern.quote("+")).append("|")
				.append(Pattern.quote("*")).append("|")
				.append(Pattern.quote("%7E")).append("|")
				.append(Pattern.quote("%2F"));

		ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
	}

	public static void printHttpRequest(HttpRequestBase request) {
		log.debug("the http request info:");
		for (Header s : request.getAllHeaders()) {
			log.debug("headers:" + s.getName() + " " + s.getValue());
		}
		log.debug("requestline:" + request.getRequestLine().getMethod() + " "
				+ request.getRequestLine().getUri() + " "
				+ request.getRequestLine().getProtocolVersion());
	}

	
	public static Map<String,String> convertAcl2Headers(AccessControlList acl)
	{
		Map<String,String> headers = new HashMap<String,String>();
		
		List<String> grants_fullcontrol= new ArrayList<String>();
		List<String> grants_read= new ArrayList<String>();
		List<String> grants_write= new ArrayList<String>();
		if(acl.getGrants()==null)
			throw ClientIllegalArgumentExceptionGenerator.notNull("AccessControlList.Grants");
		for(Grant grant:acl.getGrants())
		{
			if(grant==null)
				throw ClientIllegalArgumentExceptionGenerator.notNull("grant");
			if(grant.getPermission()==null)
				throw ClientIllegalArgumentExceptionGenerator.notNull("grant.permission");
			if(grant.getGrantee()==null)
				throw ClientIllegalArgumentExceptionGenerator.notNull("grant.grantee");

			String id = grant.getGrantee().getIdentifier();
			if(StringUtils.isBlank(id)){
				throw ClientIllegalArgumentExceptionGenerator.notNull("grant.grantee.identifier");
			}
			if(grant.getPermission().equals(Permission.FullControl))
			{
				grants_fullcontrol.add(grant.getGrantee().getTypeIdentifier()+"=\""+id+"\"");
			}
			else if(grant.getPermission().equals(Permission.Read))
			{
				grants_read.add(grant.getGrantee().getTypeIdentifier()+"=\""+id+"\"");
			}
			else if(grant.getPermission().equals(Permission.Write))
			{
				grants_write.add(grant.getGrantee().getTypeIdentifier()+"=\""+id+"\"");
			}
		}
		if(grants_fullcontrol.size()>0)
		{
			headers.put(HttpHeaders.GrantFullControl.toString(),StringUtils.join(grants_fullcontrol,","));
		}
		if(grants_read.size()>0)
		{
			headers.put(HttpHeaders.GrantRead.toString(),StringUtils.join(grants_read,","));
		}
		if(grants_write.size()>0)
		{
			headers.put(HttpHeaders.GrantWrite.toString(),StringUtils.join(grants_write,","));
		}
		return headers;
	}
	public static String convertAdps2String(List<Adp> fops){
		StringBuffer fopStringBuffer = new StringBuffer();
		for(Adp fop : fops){
			fopStringBuffer.append(fop.getCommand());
			if(!(StringUtils.isBlank(fop.getBucket())&&StringUtils.isBlank(fop.getKey()))){
				if(StringUtils.isBlank(fop.getBucket())){
					fopStringBuffer.append(String.format("|tag=saveas&object=%s",Base64.encodeAsString(fop.getKey().getBytes())));
				}else if(StringUtils.isBlank(fop.getKey())){
					fopStringBuffer.append(String.format("|tag=saveas&bucket=%s",fop.getBucket()));
				}else{
					fopStringBuffer.append(String.format("|tag=saveas&bucket=%s&object=%s",fop.getBucket(),Base64.encodeAsString(fop.getKey().getBytes())));
				}
			}
			fopStringBuffer.append(";");
		}
		String fopString = fopStringBuffer.toString();
		if(fopString.endsWith(";")){
			fopString = fopString.substring(0,fopString.length()-1);
		}
		return fopString;
	}
	//encode objectkey时不会编码斜杠
	public static String urlEncode(final String value, final boolean path) {
		if (value == null) {
			return "";
		}

		try {
			String encoded = URLEncoder.encode(value, DEFAULT_ENCODING);

			Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded);
			StringBuffer buffer = new StringBuffer(encoded.length());

			while (matcher.find()) {
				String replacement = matcher.group(0);

				if ("+".equals(replacement)) {
					replacement = "%20";
				} else if ("*".equals(replacement)) {
					replacement = "%2A";
				} else if ("%7E".equals(replacement)) {
					replacement = "~";
				} else if (path && "%2F".equals(replacement)) {
					replacement = "/";
				}

				matcher.appendReplacement(buffer, replacement);
			}

			matcher.appendTail(buffer);
			encoded = buffer.toString();
			return encoded;

		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}
	public static String encodeParams(Map<String,String> params){
		List<Map.Entry<String, String>> arrayList = new ArrayList<Map.Entry<String, String>>(
				params.entrySet());
		Collections.sort(arrayList,
				new Comparator<Map.Entry<String, String>>() {
					public int compare(Entry<String, String> o1,
							Entry<String, String> o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				});
		List<String> list = new ArrayList<String>();
		for (Entry<String, String> entry : arrayList) {
			String value = null;
			//8203,直接从浏览器粘下来的字符串中可能含有这个非法字符
			String key = entry.getKey().replace(String.valueOf((char)8203),"");
			if (!StringUtils.isBlank(entry.getValue()))
				value = URLEncoder.encode(entry.getValue());
			if (value != null && !value.equals("")) {
				list.add(key + "=" + value);
			} else{
				if (RequestUtils.subResource.contains(key))
			    	list.add(key);
			}
		}

		String queryParams = StringUtils.join(list.toArray(), "&");
		return queryParams;
	}
	public static Map<String,String> convertMeta2Headers(ObjectMetadata meta){
		Map<String,String> map = new HashMap<String,String>();
		if(meta == null)
			return map;
		for(Entry<String,Object>  entry : meta.getRawMetadata().entrySet()){
			map.put(entry.getKey(),entry.getValue().toString());
		}
		// 添加user meta
		for (Entry<String, String> entry : meta.getAllUserMeta()
				.entrySet()) {
			if (entry.getKey().startsWith(Constants.userMetaPrefix))
				map.put(entry.getKey(), entry.getValue());
		}
		if (meta.getHttpExpiresDate() != null)
			map.put(
					HttpHeaders.Expires.toString(),
					DateUtils.convertDate2Str(
							meta.getHttpExpiresDate(),
							DATETIME_PROTOCOL.RFC1123).toString());
		return map;
	}
	public static Map<String,String> convertSSECustomerKey2Headers(SSECustomerKey key){
		Map<String,String> map = new HashMap<String,String>();
		if(key == null)
			return map;
		putAndCheckNotNull(map,HttpHeaders.XKssServerSideEncryptionCustomerAlgorithm.toString(),key.getAlgorithm());
		putAndCheckNotNull(map,HttpHeaders.XkssServerSideEncryptionCustomerKey.toString(),key.getBase64EncodedKey());
		putIfNotNull(map,HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5.toString(),key.getBase64EncodedMd5());
		if (key.getBase64EncodedKey() != null
                && key.getBase64EncodedMd5() == null) {
            String encryptionKey_b64 = key.getBase64EncodedKey();
            byte[] encryptionKey = Base64.decode(encryptionKey_b64);
            map.put(HttpHeaders.XkssServerSideEncryptionCustomerKeyMD5.toString(),
                    Md5Utils.md5AsBase64(encryptionKey));
        }
		return map;
	}
	public static Map<String,String> convertSSEKssKMSParams2Headers(SSEKssKMSParams params){
		Map<String,String> map = new HashMap<String,String>();
		if(params == null)
			return map;
		map.put(HttpHeaders.XKssServerSideEncryption.toString(),Constants.aws?"awz:kms":"kss:kms");
		if(!StringUtils.isBlank(params.getKeyId()))
			map.put(HttpHeaders.XKssServerSideEncryptionKMSKeyId.toString(),params.getKeyId());
		return map;
	}
	public static void putIfNotNull(Map<String,String> dest,String key,String value){
		if(!StringUtils.isBlank(value))
			dest.put(key, value);
	}
	public static void putAndCheckNotNull(Map<String,String> dest,String key,String value){
		if(!StringUtils.isBlank(value))
			dest.put(key, value);
		else
			throw ClientIllegalArgumentExceptionGenerator.notNullInCondition("SSECustomerKey."+key,"SSECustomerKey is not null");
	}


	public static Map<? extends String, ? extends String> convertCopySourceSSECustomerKey2Headers(
			SSECustomerKey key) {
		Map<String,String> map = new HashMap<String,String>();
		if(key == null)
			return map;
		putAndCheckNotNull(map,HttpHeaders.XKssCPSourceServerSideEncryptionCustomerAlgorithm.toString(),key.getAlgorithm());
		putAndCheckNotNull(map,HttpHeaders.XkssCPSourceServerSideEncryptionCustomerKey.toString(),key.getBase64EncodedKey());
		putIfNotNull(map,HttpHeaders.XkssCPSourceServerSideEncryptionCustomerKeyMD5.toString(),key.getBase64EncodedMd5());
		if (key.getBase64EncodedKey() != null
                && key.getBase64EncodedMd5() == null) {
            String encryptionKey_b64 = key.getBase64EncodedKey();
            byte[] encryptionKey = Base64.decode(encryptionKey_b64);
            map.put(HttpHeaders.XkssCPSourceServerSideEncryptionCustomerKeyMD5.toString(),
                    Md5Utils.md5AsBase64(encryptionKey));
        }
		return map;
	}
}
