package com.ksyun.ks3.unit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.utils.Base64;
import com.ksyun.ks3.utils.Md5Utils;

public class TestUtils extends BaseTest{
	public static void makeFile(String filename,long size){
		String content = getRandomString(size);
		makeFile(filename,content);
	}
	public static void makeFile(String filename,String content){
		File file = new File(filename);
		try{
			FileWriter writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static String getRandomString(long length) { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*():\"<>?,./";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }   
	public static String rangeGetMD5(Ks3 client,String bucket,String key) throws IOException{
		long maxLength = 1024*1024*5;
		long minLength = 1;
		long minPart = 4;
		long length = client.headObject(bucket, key).getObjectMetadata().getContentLength();
		if(length < maxLength*minPart)
			maxLength = length / minPart;
		if(maxLength < minLength)
			maxLength = minLength+1;
		List<String> keys = new ArrayList<String>();
		for(long current = 0l;current <= length;){
			long block = (long)(Math.random()*(minLength-maxLength+1))+maxLength;
			GetObjectResult result = rangeGet(client,bucket,key,current,current+block);
			String filename = dir+filedown+"_"+current+"-"+(current+block);
			writeToFile(result.getObject().getObjectContent(),new File(filename));
			keys.add(filename);
			current+=(block+1);
		}
		mergeFiles(dir+filedown,keys.toArray());
		String actual = Base64.encodeAsString(Md5Utils.computeMD5Hash(new File(dir+filedown)));
		new File(dir+filedown).delete();
		return actual;
	}
	private static GetObjectResult rangeGet(Ks3 client,String bucket,String key,long begin,long end){
		GetObjectRequest request = new GetObjectRequest(bucket,key);
		request.setRange(begin,end);
		return client.getObject(request);
	}
	protected static void mergeFiles(String outFile, Object[] files) {
		new File(outFile).delete();
		FileChannel outChannel = null;
		try {
			outChannel = new FileOutputStream(outFile).getChannel();
			for (Object f : files) {
				FileChannel fc = new FileInputStream(f.toString()).getChannel();
				ByteBuffer bb = ByteBuffer.allocate(8192);
				while (fc.read(bb) != -1) {
					bb.flip();
					outChannel.write(bb);
					bb.clear();
				}
				fc.close();
				new File(f.toString()).delete();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (outChannel != null) {
					outChannel.close();
				}
			} catch (IOException ignore) {
			}
		}
	}
	protected static void writeToFile(InputStream content,File file) throws IOException{
		OutputStream os = new FileOutputStream(file);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		try {
			while ((bytesRead = content.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				content.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void rangeGetToFileWithThreads(final Ks3 client,final String bucket,final String key,final String file) throws IOException{
		long maxLength = 1024*1024*5;
		long minLength = 1;
		int minPart = 4;
		long length = client.headObject(bucket, key).getObjectMetadata().getContentLength();
		if(length < maxLength*minPart)
			maxLength = length / minPart;
		if(maxLength < minLength)
			maxLength = minLength+1;
		
		final ExecutorService pool = Executors.newFixedThreadPool(minPart);
		final List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
		final List<String> keys = new ArrayList<String>();
		for(long current = 0l,i =0;current <= length;i++){
			long block = (long)(Math.random()*(minLength-maxLength+1))+maxLength;
			
			final long cuIndex = current;
			final long curBlock = block;
			final int curPartNum = (int)i;
			Thread t = new Thread() {
				@Override
				public void run() {
					try{
						GetObjectResult result = rangeGet(client,bucket,key,cuIndex,cuIndex+curBlock-1);
						String filename = file+"_"+cuIndex+"-"+(cuIndex+curBlock-1);
						try {
							writeToFile(result.getObject().getObjectContent(),new File(filename));
						} catch (IOException e) {
							e.printStackTrace();
						}
						int index = curPartNum - keys.size();
						for(;index>=0;index--)
							keys.add(null);
						keys.set((int)curPartNum, filename);
					}
					catch(RuntimeException e){
						exceptions.add(e);
					}
				}
			};
			if(exceptions.size()>0){
				pool.shutdownNow();
				break;
			}
			pool.execute(t);
			current+=block;
		}
		pool.shutdown();
		for (;;) {
			if (pool.isTerminated())
				break;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mergeFiles(file,keys.toArray());
		if(exceptions.size()>0)
			throw exceptions.get(0);	
	}
	public static void rangeGetToFile(Ks3 client,String bucket,String key,String file) throws IOException{
		long maxLength = 1024*1024*5;
		long minLength = 1;
		long minPart = 4;
		long length = client.headObject(bucket, key).getObjectMetadata().getContentLength();
		if(length < maxLength*minPart)
			maxLength = length / minPart;
		if(maxLength < minLength)
			maxLength = minLength+1;
		List<String> keys = new ArrayList<String>();
		for(long current = 0l;current <= length;){
			long block = (long)(Math.random()*(minLength-maxLength+1))+maxLength;
			GetObjectResult result = rangeGet(client,bucket,key,current,current+block);
			String filename = file+"_"+current+"-"+(current+block);
			writeToFile(result.getObject().getObjectContent(),new File(filename));
			keys.add(filename);
			current+=(block+1);
		}
		mergeFiles(file,keys.toArray());
		
	}
}
