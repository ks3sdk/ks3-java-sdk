package com.ksyun.ks3.service.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ksyun.ks3.dto.Ks3ObjectSummary;
import com.ksyun.ks3.dto.ObjectListing;
import com.ksyun.ks3.service.Ks3Client;
import com.ksyun.ks3.service.request.ListObjectsRequest;

public class CopyBucket {
	private int maxThreads = 30;
	private Ks3Client client;
	public CopyBucket(Ks3Client client){
		this.client = client;
	}
	public CopyBucket(Ks3Client client,int maxThreads){
		this.maxThreads = maxThreads;
		this.client = client;
	}
	public CopyBucketResult doCopy(final String sourceBucket,final String destBucket){
		final CopyBucketResult result = new CopyBucketResult();
		ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
		
		ListObjectsRequest listReq = new ListObjectsRequest(sourceBucket);
		do{
			final ObjectListing objects = client.listObjects(listReq);
			listReq.setMarker(objects.getObjectSummaries().get(objects.getObjectSummaries().size()-1).getKey());
			Thread t = new Thread() {
				@Override
				public void run() {
					for(Ks3ObjectSummary obj : objects.getObjectSummaries()){
						try{
							client.copyObject(destBucket, obj.getKey(), sourceBucket, obj.getKey());
							result.getSuccess().add(obj.getKey());
						}catch(Exception e){
							e.printStackTrace();
							result.getError().put(obj.getKey(),e);
						}
					}
				}
			};
			pool.execute(t);
			if(!objects.isTruncated())
				break;
		}while(true);
		
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
		return result;
	}
}
