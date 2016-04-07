package com.ksyun.ks3.service.request;

import java.io.File;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNull;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notNullInCondition;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.notCorrect;
import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.between;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ksyun.ks3.InputSubStream;
import com.ksyun.ks3.LengthCheckInputStream;
import com.ksyun.ks3.MD5DigestCalculatingInputStream;
import com.ksyun.ks3.RepeatableFileInputStream;
import com.ksyun.ks3.RepeatableInputStream;
import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.SSECustomerKey;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.client.ClientFileNotFoundException;
import com.ksyun.ks3.http.HttpHeaders;
import com.ksyun.ks3.http.HttpMethod;
import com.ksyun.ks3.http.Request;
import com.ksyun.ks3.utils.HttpUtils;
import com.ksyun.ks3.utils.Md5Utils;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月23日 上午11:17:36
 * 
 * @description 分块上传时，Upload Part的请求
 *              <p>
 *              可以通过setContentMD5()指定MD5值，使其在服务端进行md5值校验，否则只会在客户端校验(区别:
 *              前者校验失败不会上传成功)
 *              </p>
 *              <p>
 *              支持提供文件对象进行分块上传，请使用UploadPartRequest(String bucketname, String
 *              objectkey, String uploadId, int partNumber, File file, long
 *              partsize, long fileoffset)
 *              </p>
 *              <p>
 *              支持提供已经切分好的流进行分块上传，请提供流的长度，请使用 UploadPartRequest(String
 *              bucketname, String objectkey, String uploadId, int partNumber,
 *              InputStream content, long partSize)
 *              </p>
 * 
 **/
public class UploadPartRequest extends Ks3WebServiceRequest implements SSECustomerKeyRequest{
	private static final Log log = LogFactory.getLog(UploadPartRequest.class);
	private String bucket;
	private String key;

	/**
	 * 由init multipart upload获取到的uploadId
	 */
	private String uploadId;
	/**
	 * 分块上传时对块的编号，1-10000，请保证partNumber是连续的
	 */
	private int partNumber;
	/**
	 * 要上传的文件
	 */
	private File file;
	/**
	 * 要上传的数据流
	 */
	private InputStream inputStream;
	/**
	 * 当前块的大小，文件的时候最后一块大小不一定要准确，但是流的时候需要准确
	 */
	private long partSize;
	/**
	 * 文件的时候，之前已经读取的数据量
	 */
	private long fileoffset;
	/**
	 * 是否为最后一块,客户端数据加密时需要指定该值
	 */
	private boolean lastPart = false;
	/**
	 * 使用用户指定的key进行服务端加密
	 */
	private SSECustomerKey sseCustomerKey;
	/**
	 * 要上传的内容的MD5值
	 */
	private String ContentMD5;
	public UploadPartRequest(String bucketname,String objectkey){
		this.bucket = bucketname;
		this.key = objectkey;
	}
	/**
	 * 
	 * @param bucketname
	 * @param objectkey
	 * @param uploadId
	 * @param partNumber
	 * @param file
	 * @param partsize
	 *            注意类型为long,块的大小，除最后一块外需要给准确数字。必须提供,最大为5G,除最后一块最小为5M。当总大小小于5M，每块的最小值为100K
	 * @param fileoffset
	 *            注意类型为long，文件中已被读取的量
	 */
	public UploadPartRequest(String bucketname, String objectkey,
			String uploadId, int partNumber, File file, long partsize,
			long fileoffset) {
		this.bucket = bucketname;
		this.key = objectkey;
		this.setUploadId(uploadId);
		this.setPartNumber(partNumber);
		this.setFile(file);
		this.partSize = partsize;
		this.setFileoffset(fileoffset);
		if(file.length() - fileoffset <= this.partSize){
			this.setLastPart(true);
		}
	}

	/**
	 * 
	 * @param bucketname
	 * @param objectkey
	 * @param uploadId
	 * @param partNumber
	 * @param content
	 *            要上传的块的inputstream,(已经切分好的块)
	 * @param partSize
	 *            content的长度,必须提供,最大为5G,除最后一块最小为5M。当总大小小于5M，每块的最小值为100K
	 */
	public UploadPartRequest(String bucketname, String objectkey,
			String uploadId, int partNumber, InputStream content, long partSize) {
		this.bucket = bucketname;
		this.key = objectkey;
		this.setUploadId(uploadId);
		this.setPartNumber(partNumber);
		this.setPartSize(partSize);
		this.inputStream = content;
	}

	@Override
	public void validateParams() throws IllegalArgumentException {
		if (StringUtils.isBlank(this.bucket))
			throw notNull("bucketname");
		if (StringUtils.isBlank(this.key))
			throw notNull("objectkey");
		if (StringUtils.isBlank(this.uploadId))
			throw notNull("uploadId");
		if (partNumber < Constants.minPartNumber
				|| partNumber > Constants.maxPartNumber)
			throw between("partNumber",String.valueOf(this.partNumber),String.valueOf(Constants.minPartNumber),String.valueOf(Constants.maxPartNumber));
		if (file == null && this.inputStream == null) {
			throw notNull(
					"file(file to upload)","inputStream(inputStream to upload)");
		} else {
			if (file != null) {
				if (this.fileoffset < 0||this.fileoffset>file.length())
					throw between("fileoffset",String.valueOf(this.fileoffset),"0",
							String.valueOf(file.length()));
			}else{
				if(this.partSize <= 0l)
					throw notNullInCondition("partSize","upload part by inputstream");
			}
		}
		if (this.partSize > Constants.maxPartSize||this.partSize==0) {
			throw between("partsize",String.valueOf(this.partSize),"1",
					String.valueOf(Constants.maxPartSize));
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

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public long getPartSize() {
		return partSize;
	}
	/**
	 * 获取实际的大小
	 * @return
	 */
	public long getInstancePartSize() {
		if(this.file!=null){
			long truesize = file.length() - fileoffset < this.partSize ? file.length()
					- fileoffset : this.partSize;
			return truesize;
		}else{
			return partSize;
		}
	}

	public void setPartSize(long partSize) {
		this.partSize = partSize;
	}

	public long getFileoffset() {
		return fileoffset;
	}

	public void setFileoffset(long fileoffset) {
		this.fileoffset = fileoffset;
	}


	public boolean isLastPart() {
		return lastPart;
	}

	public void setLastPart(boolean lastPart) {
		this.lastPart = lastPart;
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
		request.addHeader(HttpHeaders.ContentType, "application/octet-stream");
		request.addQueryParam("uploadId", this.uploadId);
		request.addQueryParam("partNumber", String.valueOf(this.partNumber));
		if (this.file != null) {
			long truesize = getInstancePartSize();
			if(file.length() - fileoffset <= this.partSize){
				this.setLastPart(true);
			}
			try {
				//文件流并没有复用，closeSourceStream需要置为true,否则文件句柄未释放
				request.setContent(new LengthCheckInputStream(new InputSubStream(new RepeatableFileInputStream(
						this.file), this.fileoffset, truesize, true),truesize,true));

			} catch (FileNotFoundException e) {
				throw new ClientFileNotFoundException(e);
			}
		}else{
			request.setContent(new LengthCheckInputStream(inputStream,this.partSize,true));
		}
		//添加服务端加密相关
		request.getHeaders().putAll(HttpUtils.convertSSECustomerKey2Headers(sseCustomerKey));
		request.addHeader(HttpHeaders.ContentLength, String.valueOf(this.getInstancePartSize()));
		if(!StringUtils.isBlank(ContentMD5)){
			request.addHeader(HttpHeaders.ContentMD5, ContentMD5);
		}
	}

	public String getContentMD5() {
		return ContentMD5;
	}

	public void setContentMD5(String contentMD5) {
		ContentMD5 = contentMD5;
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
