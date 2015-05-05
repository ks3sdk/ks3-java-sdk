
package com.ksyun.ks3.service.encryption.internal;

import java.io.File;

import com.ksyun.ks3.dto.CompleteMultipartUploadResult;
import com.ksyun.ks3.dto.CopyResult;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.InitiateMultipartUploadResult;
import com.ksyun.ks3.dto.Ks3Object;
import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.dto.PartETag;
import com.ksyun.ks3.dto.PutObjectResult;
import com.ksyun.ks3.service.request.AbortMultipartUploadRequest;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.CopyPartRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;


/**
 * An internal SPI used to implement different cryptographic modules
 * for use with the S3 encryption client.
 */
public abstract class S3CryptoModule<T extends MultipartUploadContext> {
    public abstract PutObjectResult putObjectSecurely(PutObjectRequest req);

    public abstract GetObjectResult getObjectSecurely(GetObjectRequest req);

    public abstract ObjectMetadata getObjectSecurely(GetObjectRequest req,
            File dest);

    public abstract CompleteMultipartUploadResult completeMultipartUploadSecurely(
            CompleteMultipartUploadRequest req);

    public abstract InitiateMultipartUploadResult initiateMultipartUploadSecurely(
            InitiateMultipartUploadRequest req);

    public abstract PartETag uploadPartSecurely(UploadPartRequest req);

    public abstract CopyResult copyPartSecurely(CopyPartRequest req);

    public abstract void abortMultipartUploadSecurely(AbortMultipartUploadRequest req);
}
