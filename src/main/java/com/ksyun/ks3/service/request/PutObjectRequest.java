package com.ksyun.ks3.service.request;

import java.io.File;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNullInCondition;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notCorrect;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.LengthCheckInputStream;
import com.ksyun.ks3.MD5DigestCalculatingInputStream;
import com.ksyun.ks3.RepeatableFileInputStream;
import com.ksyun.ks3.RepeatableInputStream;
import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.AccessControlList;
import com.ksyun.ks3.dto.CallBackConfiguration;
import com.ksyun.ks3.dto.CallBackConfiguration.MagicVariables;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.Adp;
import com.ksyun.ks3.dto.Grant;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.Permission;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.dto.SSEKssKMSParams;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.client.ClientFileNotFoundException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Mimetypes;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.DateUtils;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.DateUtils.DATETIME_PROTOCOL;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月22日 上午10:28:06
 * 
 * @description 上传文件的请求
 *              <p>
 *              提供通过文件来上传，请使用PutObjectRequest(String bucketname, String key,
 *              File file)这个构造函数
 *              </p>
 *              <p>
 *              提供通过流来上传，请使用PutObjectRequest(String bucketname, String
 *              key,InputStream inputStream, ObjectMetadata
 *              metadata)这个构造函数，使用时请尽量在metadata中提供content
 *              -length,否则有可能导致jvm内存溢出。可以再metadata中指定contentMD5
 *              </p>
 **/
public class PutObjectRequest extends Ks3WebServiceRequest implements SSECustomerKeyRequest{
	private static final Log log = LogFactory.getLog(PutObjectRequest.class);
	/**
	 * 目标bucket
	 */
	private String bucket;
	/**
	 * 目标key
	 */
	private String key;
	/**
	 * 要上传的文件
	 */
	private File file;
	private InputStream inputStream;
	/**
	 * 将要上传的object的元数据
	 */
	private ObjectMetadata objectMeta = new ObjectMetadata();
	/**
	 * 设置新的object的acl
	 */
	private CannedAccessControlList cannedAcl;
	/**
	 * 设置新的object的acl
	 */
	private AccessControlList acl = new AccessControlList();
	/**
	 * 设置callback
	 */
	private CallBackConfiguration callBackConfiguration;
	/**
	 * 要进行的数据处理任务
	 */
	private List<Adp> adps = new ArrayList<Adp>();
	/**
	 * 数据处理任务完成后通知的url
	 */
	private String notifyURL;
	private String redirectLocation;
	
	/**
	 * 使用用户指定的key进行服务端加密
	 */
	private SSECustomerKey sseCustomerKey;
	
	/**
	 * 
	 * @param bucketname
	 * @param key
	 * @param file
	 *            要上传的文件
	 */
	public PutObjectRequest(String bucketname, String key, File file) {
		this.bucket = bucketname;
		this.key = key;
		this.setFile(file);
	}

	/**
	 * 
	 * @param bucketname
	 * @param key
	 * @param inputStream
	 * @param metadata
	 *            请尽量提供content-length,否则可能会导致jvm内存溢出
	 */
	public PutObjectRequest(String bucketname, String key,
			InputStream inputStream, ObjectMetadata metadata) {
		if(metadata == null)
			metadata = new ObjectMetadata();
		this.bucket = bucketname;
		this.key = key;
		this.setObjectMeta(metadata);
		this.inputStream = inputStream;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if (StringUtils.isBlank(this.key))
			throw notNull("objectkey");
		if (file == null && this.inputStream == null)
			throw notNull("file", "inputStream");
		if (this.redirectLocation != null) {
			if (!this.redirectLocation.startsWith("/")
					&& !this.redirectLocation.startsWith("http://")
					&& !this.redirectLocation.startsWith("https://"))
				throw notCorrect("redirectLocation", this.redirectLocation,
						"starts with / http:// or  https://");
		}
		if (file != null) {
			if (file.length() > Constants.maxSingleUpload)
				throw between("file length ", String.valueOf(file.length()),
						String.valueOf(0),
						String.valueOf(Constants.maxSingleUpload));
		} else {
			if (this.objectMeta != null
					&& this.objectMeta.getContentLength() > Constants.maxSingleUpload)
				throw between("Content-Length",
						String.valueOf(objectMeta.getContentLength()),
						String.valueOf(0),
						String.valueOf(Constants.maxSingleUpload));
		}
		if (this.callBackConfiguration != null) {
			if (StringUtils
					.isBlank(this.callBackConfiguration.getCallBackUrl())) {
				throw notNull("callBackConfiguration.callBackUrl");
			}
		}
		if (adps != null && adps.size() > 0) {
			for (Adp adp : adps) {
				if (StringUtils.isBlank(adp.getCommand())) {
					throw notNullInCondition("adps.command", "adps is not null");
				}
			}
			if (StringUtils.isBlank(notifyURL))
				throw notNullInCondition("notifyURL", "adps is not null");
		}
	}
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	public ObjectMetadata getObjectMeta() {
		return objectMeta;
	}

	public void setObjectMeta(ObjectMetadata objectMeta) {
		this.objectMeta = objectMeta;
	}

	public CannedAccessControlList getCannedAcl() {
		return cannedAcl;
	}

	public void setCannedAcl(CannedAccessControlList cannedAcl) {
		this.cannedAcl = cannedAcl;
	}

	public AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(AccessControlList acl) {
		this.acl = acl;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	public CallBackConfiguration getCallBackConfiguration() {
		return callBackConfiguration;
	}

	public void setCallBackConfiguration(
			CallBackConfiguration callBackConfiguration) {
		this.callBackConfiguration = callBackConfiguration;
	}

	public List<Adp> getAdps() {
		return adps;
	}

	public void setAdps(List<Adp> adps) {
		this.adps = adps;
	}

	public String getNotifyURL() {
		return notifyURL;
	}

	public void setNotifyURL(String notifyURL) {
		this.notifyURL = notifyURL;
	}

	public SSECustomerKey getSseCustomerKey() {
		return sseCustomerKey;
	}

	public void setSseCustomerKey(SSECustomerKey sseCustomerKey) {
		this.sseCustomerKey = sseCustomerKey;
	}

	@Override
	public void buildRequest(Request request) {
		request.setMethod(HttpMethod.PUT);
		request.setBucket(bucket);
		request.setKey(key);
		request.addHeader(HttpHeaders.ContentType,"application/octet-stream");
		if (this.objectMeta == null)
			this.objectMeta = new ObjectMetadata();
		
		InputStream input= null;
		/**
		 * 设置request body meta
		 */
		if (file != null) {
			try {
				input = new RepeatableFileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new ClientFileNotFoundException(e);
			}
			if (StringUtils.isBlank(objectMeta.getContentType()))
				objectMeta.setContentType(Mimetypes.getInstance().getMimetype(
						file));
			long length = file.length();
			objectMeta.setContentLength(length);
			try {
				String contentMd5_b64 = Md5Utils.md5AsBase64(file);
				this.objectMeta.setContentMD5(contentMd5_b64);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				if(input!=null)
					try {
						input.close();
					} catch (IOException e1) {
					}
				throw new ClientFileNotFoundException(e);
			} catch (IOException e) {
				if(input!=null)
					try {
						input.close();
					} catch (IOException e1) {
					}
				throw new Ks3ClientException("计算文件的MD5值出错 (" + e + ")", e);
			}
		}else{
			input = inputStream;
		}
		long length = objectMeta.getContentLength();
		if(length > 0)
			request.setContent(new LengthCheckInputStream(input,length,false));
		else{
			request.setContent(input);
		}
		// 根据object key匹配content-type
		if (StringUtils.isBlank(objectMeta.getContentType()))
			objectMeta.setContentType(Mimetypes.getInstance().getMimetype(
					key));
		//添加元数据
		request.getHeaders().putAll(HttpUtils.convertMeta2Headers(objectMeta));
		//添加服务端加密相关
		request.getHeaders().putAll(HttpUtils.convertSSECustomerKey2Headers(sseCustomerKey));
		// acl
		if (this.cannedAcl != null) {
			request.addHeader(HttpHeaders.CannedAcl.toString(),
					cannedAcl.toString());
		}
		if (this.acl != null) {
			request.getHeaders().putAll(HttpUtils.convertAcl2Headers(acl));
		}
		if (this.redirectLocation != null) {
			request.addHeader(HttpHeaders.XKssWebsiteRedirectLocation,
					this.redirectLocation);
		}
		if (this.callBackConfiguration != null) {
			request.addHeader(HttpHeaders.XKssCallbackUrl,
					callBackConfiguration.getCallBackUrl());
			StringBuffer body = new StringBuffer();
			if (callBackConfiguration.getBodyMagicVariables() != null) {
				for (Entry<String, MagicVariables> mvs : callBackConfiguration
						.getBodyMagicVariables().entrySet()) {
					body.append(mvs.getKey() + "=${" + mvs.getValue() + "}&");
				}
			}
			if (callBackConfiguration.getBodyKssVariables() != null) {
				for (Entry<String, String> mvs : callBackConfiguration
						.getBodyKssVariables().entrySet()) {
					body.append(mvs.getKey() + "=${kss-" + mvs.getKey() + "}&");
					request.addHeader("kss-" + mvs.getKey(), mvs.getValue());
				}
			}
			String bodyString = body.toString();
			if (bodyString.endsWith("&")) {
				bodyString = bodyString.substring(0, bodyString.length() - 1);
			}
			request.addHeader(HttpHeaders.XKssCallbackBody, bodyString);
		}
		if (this.adps != null && adps.size() > 0) {
			request.addHeader(HttpHeaders.AsynchronousProcessingList,
					URLEncoder.encode(HttpUtils.convertAdps2String(adps)));
			if (!StringUtils.isBlank(notifyURL))
				request.addHeader(HttpHeaders.NotifyURL, HttpUtils.urlEncode(notifyURL,false));
		}
	}
	@Override
	public void onFinally(){
		if(this.inputStream != null){
			try{
				inputStream.close();
			}catch(Exception e){
				log.error("put object on finally close input stream err,"+e.getMessage());
			}
		}
	}
}
