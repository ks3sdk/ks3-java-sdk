package com.ksyun.ks3.service.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.ksyun.ks3.exception.client.ClientIllegalArgumentExceptionGenerator.*;

import com.ksyun.ks3.config.Constants;
import com.ksyun.ks3.dto.CannedAccessControlList;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.exception.client.ClientFileNotFoundException;
import com.ksyun.ks3.exception.serviceside.NotFoundException;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.Ks3Client;
import com.ksyun.ks3.service.encryption.Ks3EncryptionClient;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.StringUtils;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2015年4月3日 下午1:28:32
 * 
 * @description 对{@link Ks3Client}的封装,主要用于文件上传
 **/
public class Ks3UploadClient {
	private CannedAccessControlList acl;
	// 上传单个文件时最多启动的线程数(注：批量上传时，实际启动的线程数为multipartMaxThread*batchUploadThread)
	private int multipartMaxThread;
	// 批量上传时最多启动的线程数(注：批量上传时，实际启动的线程数为multipartMaxThread*batchUploadThread)
	private int batchUploadThread;
	// 批量校验是否存在时最多启动的线程数
	private int batchCheckThread;

	private boolean isEncryptionClient = false;

	private static final Log log = LogFactory.getLog(Ks3UploadClient.class);
	Ks3 client = null;

	public Ks3UploadClient(Ks3 client) {
		this(client, 5, 10, 30, CannedAccessControlList.Private);
	}

	/**
	 * 
	 * @param client
	 * @param multipartMaxThread
	 *            上传单个文件时最多启动的线程数
	 * @param batchUploadThread
	 *            批量上传时最多启动的线程数
	 * @param batchCheckThread
	 *            批量校验时最多启动的线程数
	 */
	public Ks3UploadClient(Ks3 client, int multipartMaxThread,
			int batchUploadThread, int batchCheckThread) {
		this(client, multipartMaxThread, batchUploadThread, batchCheckThread,
				CannedAccessControlList.Private);
	}

	/**
	 * @param client
	 * @param acl
	 *            上传上去的文件的访问权限
	 */
	public Ks3UploadClient(Ks3 client, CannedAccessControlList acl) {
		this(client, 5, 10, 30, acl);
	}

	/**
	 * 
	 * @param client
	 * @param multipartMaxThread
	 *            上传单个文件时最多启动的线程数
	 * @param batchUploadThread
	 *            批量上传时最多启动的线程数
	 * @param batchCheckThread
	 *            批量校验时最多启动的线程数
	 * @param acl
	 *            上传上去的文件的访问权限
	 */
	public Ks3UploadClient(Ks3 client, int multipartMaxThread,
			int batchUploadThread, int batchCheckThread,
			CannedAccessControlList acl) {
		this.client = client;
		this.multipartMaxThread = multipartMaxThread;
		this.batchUploadThread = batchUploadThread;
		this.batchCheckThread = batchCheckThread;
		this.acl = acl;
		if (client instanceof Ks3EncryptionClient) {
			this.isEncryptionClient = true;
		}
	}

	public CannedAccessControlList getAcl() {
		return acl;
	}

	public void setAcl(CannedAccessControlList acl) {
		this.acl = acl;
	}

	public int getMultipartMaxThread() {
		return multipartMaxThread;
	}

	public void setMultipartMaxThread(int multipartMaxThread) {
		this.multipartMaxThread = multipartMaxThread;
	}

	public int getBatchUploadThread() {
		return batchUploadThread;
	}

	public void setBatchUploadThread(int batchUploadThread) {
		this.batchUploadThread = batchUploadThread;
	}

	public int getBatchCheckThread() {
		return batchCheckThread;
	}

	public void setBatchCheckThread(int batchCheckThread) {
		this.batchCheckThread = batchCheckThread;
	}

	public Ks3 getClient() {
		return client;
	}

	public void setClient(Ks3 client) {
		this.client = client;
	}

	/**
	 * 根据文件大小自动选择普通上传或分块上传
	 * 
	 * @param bucket
	 * @param key
	 * @param file
	 */
	public void uploadFile(String bucket, String key, File file) {
		log.debug(String.format(
				"UPLOAD_FILE:upload file %s to bucket %s key %s ",
				file.getAbsolutePath(), bucket, key));
		long length = file.length();
		if (length <= 100 * Constants.KB) {
			PutObjectRequest request = new PutObjectRequest(bucket, key, file);
			request.setCannedAcl(acl);
			client.putObject(request);
		} else {
			//客户端加密上传的时候不能使用多线程
			if(this.isEncryptionClient)
				mutipartUpload(bucket,key,file);
			else
				mutipartUploadByThreads(bucket, key, file);
		}
	}

	/**
	 * 将本地的文件系统上传到bucket下
	 * 
	 * @param bucket
	 * @param destDir
	 *            目标目录，如果是直接往bucket下上传，则设置为空
	 * @param sourceDir
	 *            本地目录
	 * @return 上传失败的文件列表
	 */
	public Map<String, File> uploadDir(String bucket, String destDir,
			File sourceDir) {
		return uploadDir(bucket, destDir, sourceDir, false);
	}

	/**
	 * 将本地的文件系统上传到bucket下
	 * 
	 * @param bucket
	 * @param destDir
	 *            目标目录,如果是直接往bucket下上传，则设置为空
	 * @param sourceDir
	 *            本地目录
	 * @param check
	 *            是否先校验bucket里是否有该文件再决定是否上传
	 * @return 上传失败的文件列表
	 */
	public Map<String, File> uploadDir(String bucket, String destDir,
			File sourceDir, boolean check) {
		int maxTry = 3;
		// 上传失败的文件列表
		Map<String, File> toUpload = new ConcurrentHashMap<String, File>();
		generate(toUpload, destDir, sourceDir);

		Map<String, File> error = toUpload;
		int i = 0;
		do {
			log.debug("UPLOAD_DIR_BATCH_START:batch upload rand " + i
					+ ",bucket " + bucket + ",destDir " + destDir
					+ ",sourceDir " + sourceDir + ",count:" + error.size());
			error = this.batchUpload(bucket, error, check);
			log.debug("UPLOAD_DIR_BATCH_END:batch upload rand " + i + ",bucket "
					+ bucket + ",destDir " + destDir + ",sourceDir "
					+ sourceDir + ",count:" + error.size());
			i++;
		} while (error.size() > 0 && i < maxTry);
		return error;
	}

	private void generate(Map<String, File> toUpload, String destDir,
			File sourceDir) {
		if (destDir == null)
			destDir = "";
		if (!StringUtils.isBlank(destDir) && !destDir.endsWith("/"))
			throw notCorrect("destDir", destDir, "ends with /");
		String baseKey = destDir + StringUtils.getFileName(sourceDir);
		if (sourceDir.isDirectory()) {
			File[] files = sourceDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						generate(toUpload, baseKey + "/", file);
					} else {
						String key = baseKey + "/"
								+ StringUtils.getFileName(file);
						if (StringUtils.isBlank(baseKey))
							key = StringUtils.getFileName(file);
						log.debug("found key :" + key);
						toUpload.put(key, file);
					}
				}
			}
		} else {
			log.debug("found key :" + baseKey);
			toUpload.put(baseKey, sourceDir);
		}
	}

	/**
	 * 批量上传
	 * 
	 * @param bucket
	 *            目标bucket
	 * @param files
	 *            key:保存在KS3上的key，value：对应的文件
	 * @return 上传失败的文件列表
	 */
	public Map<String, File> batchUpload(String bucket, Map<String, File> files) {
		return batchUpload(bucket, files, false, batchUploadThread);
	}

	/**
	 * 批量上传
	 * 
	 * @param bucket
	 *            目标bucket
	 * @param files
	 *            key:保存在KS3上的key，value：对应的文件
	 * @param check
	 *            是否先校验bucket里是否有该文件再决定是否上传
	 * @return 上传失败的文件列表
	 */
	public Map<String, File> batchUpload(String bucket,
			Map<String, File> files, boolean check) {
		return batchUpload(bucket, files, check, batchUploadThread);
	}

	/**
	 * 批量上传
	 * 
	 * @param bucket
	 *            目标bucket
	 * @param files
	 *            key:保存在KS3上的key，value：对应的文件
	 * @param maxThreads
	 *            最大允许启动的线程数
	 * @return 上传失败的文件列表
	 */
	public Map<String, File> batchUpload(String bucket,
			Map<String, File> files, int maxThreads) {
		return batchUpload(bucket, files, false, batchUploadThread);
	}

	/**
	 * 批量上传
	 * 
	 * @param bucket
	 *            目标bucket
	 * @param files
	 *            key:保存在KS3上的key，value：对应的文件
	 * @param check
	 *            是否先校验bucket里是否有该文件再决定是否上传
	 * @param maxThreads
	 *            最大允许启动的线程数
	 * @return 上传失败的文件列表
	 */
	public Map<String, File> batchUpload(final String bucket,
			Map<String, File> files, final boolean check, int maxThreads) {
		int total = files.size();
		if (total < maxThreads)
			maxThreads = total;
		final Map<String, File> faild = new ConcurrentHashMap<String, File>();
		if (total == 0) {
			return faild;
		}
		ExecutorService pool = Executors.newFixedThreadPool(maxThreads);

		// 上传块的线程
		for (final Entry<String, File> enrty : files.entrySet()) {
			Thread t = new Thread() {
				@Override
				public void run() {
					boolean exists = false;
					if (check) {
						// 查看是否已经存在
						try {
							client.headObject(bucket, enrty.getKey());
							exists = true;
						} catch (Exception e) {
							exists = false;
						}
					}
					if (!exists) {
						try {
							uploadFile(bucket, enrty.getKey(), enrty.getValue());
						} catch (Exception e) {
							log.error(String
									.format("UPLOAD_FILE_ERROR:upload file %s to bucket %s key %s error %s",
											enrty.getValue().getAbsolutePath(),
											bucket, enrty.getKey(), e));
							faild.put(enrty.getKey(), enrty.getValue());
						}
					} else
						log.debug(String
								.format("UPLOAD_FILE_SKIPPED:upload file %s to bucket %s key %s skipped as it exists",
										enrty.getValue().getAbsolutePath(),
										bucket, enrty.getKey()));
				}
			};
			pool.execute(t);
		}
		pool.shutdown();
		for (;;) {
			if (pool.isTerminated())
				break;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return faild;
	}

	/**
	 * 
	 * 校验keys是否在服务器上存在
	 * 
	 * @param bucket
	 * @param keys
	 *            要校验的key列表
	 * @return 不存在的key
	 */
	public List<String> batchCheck(final String bucket, List<String> keys) {
		return batchCheck(bucket, keys, this.batchCheckThread);
	}

	/**
	 * 校验keys是否在服务器上存在
	 * 
	 * @param bucket
	 * @param keys
	 *            要校验的key列表
	 * @param maxThreads
	 *            最大启动的线程数
	 * @return 不存在的key
	 */
	public List<String> batchCheck(final String bucket, List<String> keys,
			int maxThreads) {
		final int maxRetry = 3;
		final List<String> notFound = new ArrayList<String>();
		int total = keys.size();
		if (total < maxThreads)
			maxThreads = total;
		if (total == 0) {
			return notFound;
		}
		ExecutorService pool = Executors.newFixedThreadPool(maxThreads);

		for (final String key : keys) {
			Thread t = new Thread() {
				@Override
				public void run() {
					int faild = 0;
					try {
						log.debug(String
								.format("CHECK_EXISTS:check key %s , bucket %s,times %s",
										key, bucket, faild));
						client.headObject(bucket, key);
					} catch (NotFoundException e) {
						notFound.add(key);
					} catch (Exception e) {
						faild++;
						if (faild <= maxRetry) {
							run();
						} else {
							// 连续多次出现异常则认为不存在
							notFound.add(key);
						}
					}
				}
			};
			pool.execute(t);
		}
		pool.shutdown();
		for (;;) {
			if (pool.isTerminated())
				break;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return notFound;
	}

	/**
	 * 检验目录中的文件是否已经都上传到服务器上
	 * 
	 * @param bucket
	 * @param destDir
	 *            目标目录,如果是直接往bucket下上传，则设置为空
	 * @param sourceDir
	 *            本地目录
	 * @return 不存在的key
	 */
	public List<String> checkDir(String bucket, String destDir, File sourceDir) {
		return checkDir(bucket, destDir, sourceDir, this.batchCheckThread);
	}

	/**
	 * 检验目录中的文件是否已经都上传到服务器上
	 * 
	 * @param bucket
	 * @param destDir
	 *            目标目录,如果是直接往bucket下上传，则设置为空
	 * @param sourceDir
	 *            本地目录
	 * @param maxThreads
	 *            最多启动的线程数
	 * @return 不存在的key
	 */
	public List<String> checkDir(String bucket, String destDir, File sourceDir,
			int maxThreads) {
		Map<String, File> toUpload = new ConcurrentHashMap<String, File>();
		generate(toUpload, destDir, sourceDir);
		return batchCheck(bucket, new ArrayList<String>(toUpload.keySet()),
				maxThreads);
	}

	/**
	 * 使用分块上传将一个文件上传到指定的bucket
	 * 
	 * @param bucket
	 * @param key
	 * @param file
	 */
	public void mutipartUploadByThreads(String bucket, String key, File file) {
		mutipartUploadByThreads(bucket, key, file, multipartMaxThread);
	}

	/**
	 * 使用分块上传将一个文件上传到指定的bucket
	 * 
	 * @param bucket
	 * @param key
	 * @param file
	 * @param maxThreads
	 *            允许最多启动的线程数
	 */
	public void mutipartUploadByThreads(String bucket, String key, File file,
			int maxThreads) {
		long length = file.length();
		long partSize = 0l;
		if (length == 0)
			client.putObject(bucket, key, file);
		if (length < 5 * Constants.MB) {
			partSize = 100 * Constants.KB;
		} else {
			partSize = 5 * Constants.MB;
		}
		int threads = (int) (length / partSize)
				+ (length % partSize == 0 ? 0 : 1);
		if (threads > maxThreads)
			threads = maxThreads;
		mutipartUploadByThreads(bucket, key, file, partSize, threads);
	}

	/**
	 * 使用分块上传将一个文件上传到指定的bucket
	 * 
	 * @param bucket
	 * @param key
	 * @param file
	 * @param partSize
	 *            指定每块的大小
	 * @param threads
	 *            指定使用多少个线程
	 */
	public void mutipartUploadByThreads(final String bucket, final String key,
			final File file, final long partSize, int threads) {
		long length = file.length();
		final int maxRetry = 3;
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(
				bucket, key);
		request.setCannedAcl(acl);
		final InitiateMultipartUploadResult initResult = client
				.initiateMultipartUpload(request);
		final List<PartETag> partEtags = new ArrayList<PartETag>();

		final int partnums = (int) (length / partSize)
				+ (length % partSize == 0 ? 0 : 1);

		final ExecutorService pool = Executors.newFixedThreadPool(threads);
		final Map<String, Object> shouldComplete = new ConcurrentHashMap<String, Object>();
		shouldComplete.put("flag", true);

		// 上传块的线程
		for (int i = 0; i < partnums; i++) {
			final int partNum = i;
			Thread t = new Thread() {
				int failed = 0;

				@Override
				public void run() {
					try {
						UploadPartRequest upRequest = new UploadPartRequest(
								bucket, key, initResult.getUploadId(),
								partNum + 1, file, partSize, partSize * partNum);
						if (partNum == partnums - 1) {
							upRequest.setLastPart(true);
						}
						PartETag upResult = client.uploadPart(upRequest);
						partEtags.add(upResult);
					} catch (RuntimeException e) {
						failed++;
						if (failed <= maxRetry)
							run();
						else {
							String errorMsg = String
									.format("MULTIPART_UPLOAD:bucket %s key %s file %s uploadid %s partNumber %s upload fail after %s retrys",
											bucket, key,
											file.getAbsolutePath(),
											initResult.getUploadId(),
											partNum + 1, maxRetry);
							log.error(errorMsg);
							shouldComplete.put("flag", false);
							shouldComplete.put("cause", e);
							pool.shutdownNow();
						}

					}
				}
			};
			if((Boolean)shouldComplete.get("flag")==false){
				pool.shutdownNow();
				break;
			}
			pool.execute(t);
		}
		pool.shutdown();
		for (;;) {
			if (pool.isTerminated())
				break;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if ((Boolean) shouldComplete.get("flag")) {
			client.completeMultipartUpload(bucket, key,
					initResult.getUploadId(), partEtags);
		} else {
			client.abortMultipartUpload(bucket, key, initResult.getUploadId());
			throw (RuntimeException) shouldComplete.get("cause");
		}
	}

	/**
	 * 使用单线程进行分块上传
	 * 
	 * @param bucket
	 * @param key
	 * @param file
	 */
	public void mutipartUpload(String bucket, String key, File file) {
		long length = file.length();
		long partSize = 0l;
		if (length == 0)
			client.putObject(bucket, key, file);
		if (length < 5 * Constants.MB) {
			partSize = 100 * Constants.KB;
		} else {
			partSize = 5 * Constants.MB;
		}
		mutipartUpload(bucket, key, file, partSize);
	}

	/**
	 * 使用单线程进行分块上传
	 * 
	 * @param bucket
	 * @param key
	 * @param file
	 * @param partSize
	 *            每块的大小
	 */
	public void mutipartUpload(String bucket, String key, File file,
			long partSize) {
		long length = file.length();
		final int maxRetry = 3;
		InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(
				bucket, key);
		request.setCannedAcl(acl);
		final InitiateMultipartUploadResult initResult = client
				.initiateMultipartUpload(request);
		final List<PartETag> partEtags = new ArrayList<PartETag>();

		final int partnums = (int) (length / partSize)
				+ (length % partSize == 0 ? 0 : 1);
		boolean success = true;
		// 上传块的线程
		for (int i = 0; i < partnums; i++) {
			final int partNum = i;
			try {
				UploadPartRequest upRequest = new UploadPartRequest(bucket,
						key, initResult.getUploadId(), partNum + 1, file,
						partSize, partSize * partNum);
				if (partNum == partnums - 1) {
					upRequest.setLastPart(true);
				}
				PartETag upResult = client.uploadPart(upRequest);
				partEtags.add(upResult);
			} catch (RuntimeException e) {
				String errorMsg = String
						.format("MULTIPART_UPLOAD:bucket %s key %s file %s uploadid %s partNumber %s upload fail ",
								bucket, key, file.getAbsolutePath(),
								initResult.getUploadId(), partNum + 1, maxRetry);
				log.error(errorMsg);
				success = false;
				client.abortMultipartUpload(bucket, key, initResult.getUploadId());
				throw e;
			} 
		}
		if(success)
			client.completeMultipartUpload(bucket, key, initResult.getUploadId(),
				partEtags);
	}
}
