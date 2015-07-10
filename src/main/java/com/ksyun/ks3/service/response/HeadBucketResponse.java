package com.ksyun.ks3.service.response;
import org.apache.http.Header;
import com.ksyun.ks3.dto.HeadBucketResult;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月21日 下午2:17:06
 * 
 * @description 
 **/
public class HeadBucketResponse extends Ks3WebServiceDefaultResponse<HeadBucketResult>{

	/**
	 * 200 OK
	 * 301
	 * 403 没有权限
	 * 404不存在
	 */
	public int[] expectedStatus() {
		return new int[]{200,301,403,404};
	}

	@Override
	public void preHandle() {
		this.result = new HeadBucketResult();
		Header[]  headers = this.getHttpResponse().getAllHeaders();
		for(int i = 0;i< headers.length;i++)
		{
			this.result.getHeaders().put(headers[i].getName(),headers[i].getValue());
		}
		this.result.setStatueCode(this.getHttpResponse().getStatusLine().getStatusCode());
	}

}
