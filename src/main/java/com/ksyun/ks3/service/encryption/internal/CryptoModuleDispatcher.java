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
import com.ksyun.ks3.exception.Ks3ClientException;
import com.ksyun.ks3.exception.Ks3ServiceException;
import com.ksyun.ks3.service.encryption.S3Direct;
import com.ksyun.ks3.service.encryption.model.CryptoConfiguration;
import com.ksyun.ks3.service.encryption.model.CryptoMode;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsProvider;
import com.ksyun.ks3.service.request.AbortMultipartUploadRequest;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.CopyPartRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;

/**
 * A proxy cryptographic module used to dispatch method calls to the appropriate
 * underlying cryptographic module depending on the current configuration.
 */
public class CryptoModuleDispatcher extends S3CryptoModule<MultipartUploadContext> {
    private final CryptoMode defaultCryptoMode;
    /** Encryption only (EO) cryptographic module. */
    private final S3CryptoModuleEO eo;
    /** Authenticated encryption (AE) cryptographic module. */
    private final S3CryptoModuleAE ae;

    public CryptoModuleDispatcher(S3Direct s3,
            EncryptionMaterialsProvider encryptionMaterialsProvider,
            CryptoConfiguration cryptoConfig) {
        CryptoMode cryptoMode = cryptoConfig.getCryptoMode();
        this.defaultCryptoMode = cryptoMode == null ? CryptoMode.EncryptionOnly : cryptoMode;
        switch(defaultCryptoMode) {
            case StrictAuthenticatedEncryption:
                this.ae = new S3CryptoModuleAEStrict(s3,
                        encryptionMaterialsProvider, cryptoConfig);
                this.eo = null;
                break;
            case AuthenticatedEncryption:
                this.ae = new S3CryptoModuleAE(s3,
                        encryptionMaterialsProvider, cryptoConfig);
                this.eo = null;
                break;
            default:
                this.eo = new S3CryptoModuleEO(s3,
                        encryptionMaterialsProvider, cryptoConfig);
                this.ae = new S3CryptoModuleAE(s3,
                        encryptionMaterialsProvider, cryptoConfig);
                break;
        }
    }

    @Override
    public PutObjectResult putObjectSecurely(PutObjectRequest putObjectRequest)
            throws Ks3ClientException, Ks3ServiceException {
        return defaultCryptoMode == CryptoMode.EncryptionOnly
             ? eo.putObjectSecurely(putObjectRequest)
             : ae.putObjectSecurely(putObjectRequest)
             ;
    }

    @Override
    public GetObjectResult getObjectSecurely(GetObjectRequest req)
            throws Ks3ClientException, Ks3ServiceException {
        // AE module can handle S3 objects encrypted in either AE or OE format
        return ae.getObjectSecurely(req);
    }

    @Override
    public ObjectMetadata getObjectSecurely(GetObjectRequest req, File destinationFile)
            throws Ks3ClientException, Ks3ServiceException {
        // AE module can handle S3 objects encrypted in either AE or OE format
        return ae.getObjectSecurely(req, destinationFile);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUploadSecurely(
            CompleteMultipartUploadRequest req)
                    throws Ks3ClientException, Ks3ServiceException {
        return defaultCryptoMode == CryptoMode.EncryptionOnly 
             ? eo.completeMultipartUploadSecurely(req)
             : ae.completeMultipartUploadSecurely(req)
             ;
    }

    @Override
    public void abortMultipartUploadSecurely(AbortMultipartUploadRequest req) {
        if (defaultCryptoMode == CryptoMode.EncryptionOnly)
            eo.abortMultipartUploadSecurely(req);
        else
            ae.abortMultipartUploadSecurely(req);
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUploadSecurely(
            InitiateMultipartUploadRequest req)
                    throws Ks3ClientException, Ks3ServiceException {
        return defaultCryptoMode == CryptoMode.EncryptionOnly 
             ? eo.initiateMultipartUploadSecurely(req)
             : ae.initiateMultipartUploadSecurely(req)
             ;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>NOTE:</b> Because the encryption process requires context from block
     * N-1 in order to encrypt block N, parts uploaded with the
     * KS3EncryptionClient (as opposed to the normal KS3Client) must
     * be uploaded serially, and in order. Otherwise, the previous encryption
     * context isn't available to use when encrypting the current part.
     */
    @Override
    public PartETag uploadPartSecurely(UploadPartRequest req)
        throws Ks3ClientException, Ks3ServiceException {
        return defaultCryptoMode == CryptoMode.EncryptionOnly
             ? eo.uploadPartSecurely(req)
             : ae.uploadPartSecurely(req)
             ;
    }

    @Override
    public CopyResult copyPartSecurely(CopyPartRequest req) {
        return defaultCryptoMode == CryptoMode.EncryptionOnly 
             ? eo.copyPartSecurely(req)
             : ae.copyPartSecurely(req)
             ;
    }
}
