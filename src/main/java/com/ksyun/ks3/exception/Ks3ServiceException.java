package com.ksyun.ks3.exception;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.w3c.dom.Document;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.utils.StringUtils;
import com.ksyun.ks3.utils.XmlReader;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月14日 下午5:55:37
 * 
 * @description 当抛出该异常时表示请求正常执行，但是Ks3服务器抛出异常，详见<a
 *              href="http://ks3.ksyun.com/doc/api/index.html"
 *              >http://ks3.ksyun.com/doc/api/index.html</a>最下面
 **/
public class Ks3ServiceException extends Ks3ClientException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5225806336951827450L;
	private Log log = LogFactory.getLog(Ks3ServiceException.class);

	/** 错误码 */
	private String errorCode;
	/** 状态码 */
	private int statueCode;
	/** 期望的状态码 */
	private String expectedStatueCode;
	/** 错误 */
	private String errorMessage;
	/** 用户请求的资源 */
	private String resource;
	private String requestId;

	public Ks3ServiceException() {
		super("");
	}
	public Ks3ServiceException(HttpRequestBase request,HttpResponse response, String expected){
		super("");
		this.expectedStatueCode = expected;
		this.statueCode = response.getStatusLine().getStatusCode();
		try {
			InputStream  in =  response.getEntity().getContent();
			String xml = StringUtils.inputStream2String(in);
			log.debug(xml);
			Document document = new XmlReader(xml)
					.getDocument();
			try {
				errorMessage = document.getElementsByTagName("Message").item(0)
						.getTextContent();
			} catch (Exception e) {
				this.errorMessage = "unknow";
			}
			try {
				errorCode = document.getElementsByTagName("Code").item(0)
						.getTextContent();
			} catch (Exception e) {
				this.errorCode = "unknow";
			}
			try {
				resource = document.getElementsByTagName("Resource").item(0)
						.getTextContent();
			} catch (Exception e) {
				this.resource = "unknow";
			}
			try {
				requestId = document.getElementsByTagName("RequestId").item(0)
						.getTextContent();
			} catch (Exception e) {
				this.requestId = "unknow";
			}
		} catch (Exception e) {
		} finally {
			if(request != null)
				request.abort();
			try {
				if (response.getEntity().getContent() != null)
					response.getEntity().getContent().close();
				
			} catch (Exception e) {
			}
		}
	}
	public Ks3ServiceException(HttpResponse response, String expected) {
		this(null,response,expected);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ":" + "[RequestId:" + this.requestId
				+ ",Resource:" + resource + ",Statue code:" + this.statueCode
				+ ",Expected statue code:" + this.expectedStatueCode
				+ ",Error code:" + this.errorCode + ",Error message:"
				+ this.errorMessage + "]";
	}

	public String getErrorCode() {
		return errorCode;
	}

	public int getStatueCode() {
		return statueCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getResource() {
		return this.resource;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getExpectedStatueCode() {
		return expectedStatueCode;
	}

	public void setExpectedStatueCode(String expectedStatueCode) {
		this.expectedStatueCode = expectedStatueCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setStatueCode(int statueCode) {
		this.statueCode = statueCode;
	}

	public void setErrorMessage(String message) {
		this.errorMessage = message;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	// 将当前异常转化为com.ksyun.ks3.exception.serviceside.*下的异常
	public <X extends Ks3ServiceException> RuntimeException convert(String reqid) {
		if(!StringUtils.isBlank(reqid))
			this.setRequestId(reqid);
		if (StringUtils.isBlank(this.getErrorCode())
				|| "unknow".equals(this.getErrorCode())) {
			if(this.statueCode == 400)
				this.setErrorCode("InvalidArgument");
			else if (this.statueCode == 403)
				this.setErrorCode("AccessDenied");
			else if(this.statueCode == 404)
				this.setErrorCode("NotFound");
			else if(this.statueCode == 405)
				this.setErrorCode("MethodNotAllowed");
		}
		String error  = this.getErrorCode();
		if(!StringUtils.isBlank(error))
		    error = error.substring(0,1).toUpperCase()+error.substring(1);
		String classString = Constants.KS3_PACAKAGE + ".exception.serviceside."
				+ error + "Exception";
		try {
			@SuppressWarnings("unchecked")
			X e = (X) Class.forName(classString).newInstance();
			e.setErrorMessage(this.getErrorMessage());
			e.setErrorCode(this.getErrorCode());
			e.setExpectedStatueCode(this.getExpectedStatueCode());
			e.setRequestId(this.getRequestId());
			e.setResource(this.getResource());
			e.setStatueCode(this.getStatueCode());
			e.setStackTrace(this.getStackTrace());
			return e;
		} catch (Throwable e) {
			return this;
		}

	}
}
