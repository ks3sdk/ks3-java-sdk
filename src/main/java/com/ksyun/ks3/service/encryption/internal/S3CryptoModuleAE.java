
package com.ksyun.ks3.service.encryption.internal;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.InputSubStream;
import com.ksyun.ks3.RepeatableFileInputStream;
import com.ksyun.ks3.config.Constants;
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
import com.ksyun.ks3.service.encryption.model.CryptoStorageMode;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsProvider;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.CopyPartRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;
import com.ksyun.ks3.utils.Jackson;
/**
 * Authenticated encryption (AE) cryptographic module for the S3 encryption client.
 */
class S3CryptoModuleAE extends S3CryptoModuleBase<MultipartUploadCryptoContext> {
    static {
        // Enable bouncy castle if available
        CryptoRuntime.enableBouncyCastle();
    }
    private static final boolean IS_MULTI_PART = true;

    S3CryptoModuleAE(S3Direct s3,
            EncryptionMaterialsProvider encryptionMaterialsProvider,
            CryptoConfiguration cryptoConfig) {
        super(s3, encryptionMaterialsProvider,
                 cryptoConfig, 
                new S3CryptoScheme(ContentCryptoScheme.AES_GCM));
    }


    /**
     * Returns true if a strict encryption mode is in use in the current crypto
     * module; false otherwise.
     */
    protected boolean isStrict() {
        return false;
    }

    /**
     * @throws SecurityException
     *             if the crypto scheme used in the given content crypto
     *             material is not allowed in this crypto module.
     */
    protected void securityCheck(ContentCryptoMaterial cekMaterial,
            S3ObjectWrapper retrieved) {
        // default is no-op. Sublcass may override.
    }

    @Override
    public PutObjectResult putObjectSecurely(PutObjectRequest putObjectRequest)
            throws Ks3ClientException, Ks3ServiceException {
        appendUserAgent(putObjectRequest,Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);

        if (this.cryptoConfig.getStorageMode() == CryptoStorageMode.InstructionFile) {
            return putObjectUsingInstructionFile(putObjectRequest);
        } else {
            return putObjectUsingMetadata(putObjectRequest);
        }
    }

    private PutObjectResult putObjectUsingMetadata(PutObjectRequest req)
            throws Ks3ClientException, Ks3ServiceException {
        ContentCryptoMaterial cekMaterial = createContentCryptoMaterial(req);
        // Wraps the object data with a cipher input stream
        PutObjectRequest wrappedReq = wrapWithCipher(req, cekMaterial);
        // Update the metadata
        req.setObjectMeta(updateMetadataWithContentCryptoMaterial(
                req.getObjectMeta(), req.getFile(),
                cekMaterial));
        // Put the encrypted object into S3
        return s3.putObject(wrappedReq);
    }

    @Override
    public GetObjectResult getObjectSecurely(GetObjectRequest req)
            throws Ks3ClientException, Ks3ServiceException {
        appendUserAgent(req,Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);
        // Adjust the crypto range to retrieve all of the cipher blocks needed to contain the user's desired
        // range of bytes.
        long[] desiredRange = req.getRange();
        if (isStrict() && desiredRange  != null)
            throw new SecurityException("Range get is not allowed in strict crypto mode");
        long[] adjustedCryptoRange = EncryptionUtils.getAdjustedCryptoRange(desiredRange);
        if (adjustedCryptoRange != null)
            req.setRange(adjustedCryptoRange[0], adjustedCryptoRange[1]);
        // Get the object from S3
        GetObjectResult retrievedRet = s3.getObject(req);
        Ks3Object retrieved = null;
        if(!retrievedRet.hasContent())
        	return retrievedRet;
        else
        	retrieved = retrievedRet.getObject();
        // If the caller has specified constraints, it's possible that super.getObject(...)
        // would return null, so we simply return null as well.
        if (retrieved == null)
            return null;
        try {
            Ks3Object oj = decipher(req, desiredRange, adjustedCryptoRange, retrieved);
            GetObjectResult result = new GetObjectResult();
            result.setObject(oj);
            return result;
        } catch (RuntimeException rc) {
            // If we're unable to set up the decryption, make sure we close the
            // HTTP connection
            closeStream(retrieved.getObjectContent());
            throw rc;
        } catch (Error error) {
            closeStream(retrieved.getObjectContent());
            throw error;
        }
    }

    private void closeStream(InputStream stream){
        try {
            stream.close();
        } catch (Exception e) {
            log.debug("Safely ignoring", e);
        }
    }

    private Ks3Object decipher(GetObjectRequest req,
            long[] desiredRange, long[] cryptoRange,
            Ks3Object retrieved) {
        S3ObjectWrapper wrapped = new S3ObjectWrapper(retrieved);
        // Check if encryption info is in object metadata
        if (wrapped.hasEncryptionInfo())
            return decipherWithMetadata(desiredRange, cryptoRange, wrapped);
        // Check if encrypted info is in an instruction file
        S3ObjectWrapper instructionFile = fetchInstructionFile(req);
        if (instructionFile != null) {
            try {
                if (instructionFile.isInstructionFile()) {
                    return decipherWithInstructionFile(desiredRange, cryptoRange,
                            wrapped, instructionFile);
                }
            } finally {
                try {
                    instructionFile.getObjectContent().close();
                } catch (Exception ignore) {
                }
            }
        }
        if (isStrict()) {
            try {
                wrapped.close();
            } catch (IOException ignore) {
            }
            throw new SecurityException("S3 object with bucket name: "
                    + retrieved.getBucketName() + ", key: "
                    + retrieved.getKey() + " is not encrypted");
        }
       // The object was not encrypted to begin with. Return the object
        // without decrypting it.
        log.warn(String.format(
                "Unable to detect encryption information for object '%s' in bucket '%s'. "
                        + "Returning object without decryption.",
                retrieved.getKey(),
                retrieved.getBucketName()));
        // Adjust the output to the desired range of bytes.
        S3ObjectWrapper adjusted = adjustToDesiredRange(wrapped, desiredRange, null);
        return adjusted.getS3Object();
    }

    private Ks3Object decipherWithInstructionFile(long[] desiredRange,
            long[] cryptoRange, S3ObjectWrapper retrieved,
            S3ObjectWrapper instructionFile) {
        String json = instructionFile.toJsonString();
        @SuppressWarnings("unchecked")
        Map<String, String> instruction = Collections.unmodifiableMap(
                Jackson.fromJsonString(json, Map.class));
        ContentCryptoMaterial cekMaterial =
                ContentCryptoMaterial.fromInstructionFile(
                    instruction,
                    kekMaterialsProvider,
                    cryptoConfig.getCryptoProvider(), 
                    cryptoRange   // range is sometimes necessary to compute the adjusted IV
            );
        securityCheck(cekMaterial, retrieved);
        S3ObjectWrapper decrypted = decrypt(retrieved, cekMaterial, cryptoRange);
        // Adjust the output to the desired range of bytes.
        S3ObjectWrapper adjusted = adjustToDesiredRange(
                decrypted, desiredRange, instruction);
        return adjusted.getS3Object();
    }

    private Ks3Object decipherWithMetadata(long[] desiredRange,
            long[] cryptoRange, S3ObjectWrapper retrieved) {
        ContentCryptoMaterial cekMaterial = ContentCryptoMaterial
            .fromObjectMetadata(retrieved.getObjectMetadata(),
                kekMaterialsProvider,
                cryptoConfig.getCryptoProvider(),
                // range is sometimes necessary to compute the adjusted IV
                cryptoRange
            );
        securityCheck(cekMaterial, retrieved);
        S3ObjectWrapper decrypted = decrypt(retrieved, cekMaterial, cryptoRange);
        // Adjust the output to the desired range of bytes.
        S3ObjectWrapper adjusted = adjustToDesiredRange(
                decrypted, desiredRange, null);
        return adjusted.getS3Object();
    }

    /**
     * Adjusts the retrieved S3Object so that the object contents contain only the range of bytes
     * desired by the user.  Since encrypted contents can only be retrieved in CIPHER_BLOCK_SIZE
     * (16 bytes) chunks, the S3Object potentially contains more bytes than desired, so this method
     * adjusts the contents range.
     *
     * @param s3object
     *      The S3Object retrieved from S3 that could possibly contain more bytes than desired
     *      by the user.
     * @param range
     *      A two-element array of longs corresponding to the start and finish (inclusive) of a desired
     *      range of bytes.
     * @param instruction
     *      Instruction file in JSON or null if no instruction file is involved
     * @return
     *      The S3Object with adjusted object contents containing only the range desired by the user.
     *      If the range specified is invalid, then the S3Object is returned without any modifications.
     */
    protected final S3ObjectWrapper adjustToDesiredRange(S3ObjectWrapper s3object,
            long[] range, Map<String,String> instruction) {
        if (range == null)
            return s3object;
        // Figure out the original encryption scheme used, which can be
        // different from the crypto scheme used for decryption.
        ContentCryptoScheme encryptionScheme = s3object.encryptionSchemeOf(instruction);
        // range get on data encrypted using AES_GCM
        final long instanceLen = s3object.getObjectMetadata().getInstanceLength();
        final long maxOffset = instanceLen - encryptionScheme.getTagLengthInBits() / 8 - 1;
        if (range[1] > maxOffset) {
            range[1] = maxOffset;
            if (range[0] > range[1]) {
                // Return empty content
                try { // First let's close the existing input stream to
                      // avoid resource leakage
                    s3object.getObjectContent().close();
                } catch (IOException ignore) {
                    log.trace("", ignore);
                }
                s3object.setObjectContent(new ByteArrayInputStream(new byte[0]));
                return s3object;
            }
        }
        if (range[0] > range[1]) {
            // Make no modifications if range is invalid.
            return s3object;
        }
        try {
            AutoAbortInputStream objectContent = s3object.getObjectContent();
            InputStream adjustedRangeContents = new AdjustedRangeInputStream(objectContent, range[0], range[1]);
            s3object.setObjectContent(new AutoAbortInputStream(adjustedRangeContents, objectContent.getRequest()));
            return s3object;
        } catch (IOException e) {
            throw new Ks3ClientException("Error adjusting output to desired byte range: " + e.getMessage());
        }
    }
    
    @Override
    public ObjectMetadata getObjectSecurely(GetObjectRequest getObjectRequest, File destinationFile)
            throws Ks3ClientException, Ks3ServiceException {
        assertParameterNotNull(destinationFile,
        "The destination file parameter must be specified when downloading an object directly to a file");

        GetObjectResult s3ObjectResult = getObjectSecurely(getObjectRequest);
        if(!s3ObjectResult.hasContent())
        	return s3ObjectResult.getObject().getObjectMetadata();
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
            byte[] buffer = new byte[1024*10];
            int bytesRead;
            while ((bytesRead = s3ObjectResult.getObject().getObjectContent().read(buffer)) > -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new Ks3ClientException(
                    "Unable to store object contents to disk: " + e.getMessage(), e);
        } finally {
            try {outputStream.close();} catch (Exception e) { log.debug(e.getMessage());}
            try {s3ObjectResult.getObject().getObjectContent().close();} catch (Exception e) {log.debug(e.getMessage());}
        }

        /*
         * Unlike the standard  KS3 Client, the KS3 Encryption Client does not do an MD5 check
         * here because the contents stored in S3 and the contents we just retrieved are different.  In
         * S3, the stored contents are encrypted, and locally, the retrieved contents are decrypted.
         */

        return s3ObjectResult.getObject().getObjectMetadata();
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUploadSecurely(
            CompleteMultipartUploadRequest req) throws Ks3ClientException,
            Ks3ServiceException {
        appendUserAgent(req, Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);
        String uploadId = req.getUploadId();
        MultipartUploadCryptoContext uploadContext = multipartUploadContexts.get(uploadId);

        if (uploadContext.hasFinalPartBeenSeen() == false) {
            throw new Ks3ClientException("Unable to complete an encrypted multipart upload without being told which part was the last.  " +
                    "Without knowing which part was the last, the encrypted data in KS3 is incomplete and corrupt.");
        }

        CompleteMultipartUploadResult result = s3.completeMultipartUpload(req);

        // In InstructionFile mode, we want to write the instruction file only
        // after the whole upload has completed correctly.
        if (cryptoConfig.getStorageMode() == CryptoStorageMode.InstructionFile) {
            // Put the instruction file into S3
            s3.putObject(createInstructionPutRequest(
                    uploadContext.getBucketName(),
                    uploadContext.getKey(),
                    uploadContext.getContentCryptoMaterial()));
        }
        multipartUploadContexts.remove(uploadId);
        return result;
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUploadSecurely(
            InitiateMultipartUploadRequest req)
            throws Ks3ClientException, Ks3ServiceException {
        appendUserAgent(req, Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);
        // Generate a one-time use symmetric key and initialize a cipher to
        // encrypt object data
        ContentCryptoMaterial cekMaterial = createContentCryptoMaterial(req);
        if (cryptoConfig.getStorageMode() == CryptoStorageMode.ObjectMetadata) {
            ObjectMetadata metadata = req.getObjectMeta();
            if (metadata == null)
                metadata = new ObjectMetadata();
            // Store encryption info in metadata
            req.setObjectMeta(updateMetadataWithContentCryptoMaterial(
                    metadata, null, cekMaterial));
        }
        InitiateMultipartUploadResult result = s3.initiateMultipartUpload(req);
        MultipartUploadCryptoContext uploadContext = new MultipartUploadCryptoContext(
                req.getBucket(), req.getKey(), cekMaterial);
        multipartUploadContexts.put(result.getUploadId(), uploadContext);
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>NOTE:</b> Because the encryption process requires context from
     * previous blocks, parts uploaded with the KS3EncryptionClient (as
     * opposed to the normal KS3Client) must be uploaded serially, and in
     * order. Otherwise, the previous encryption context isn't available to use
     * when encrypting the current part.
     */
    @Override
    public PartETag uploadPartSecurely(UploadPartRequest req)
            throws Ks3ClientException, Ks3ServiceException {
        appendUserAgent(req, Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);
        final int blockSize = contentCryptoScheme.getBlockSizeInBytes();
        final boolean isLastPart = req.isLastPart();
        final String uploadId = req.getUploadId();
        final long partSize = req.getInstancePartSize();
        final boolean partSizeMultipleOfCipherBlockSize = 0 == (partSize % blockSize);
        if (!isLastPart && !partSizeMultipleOfCipherBlockSize) {
            throw new Ks3ClientException(
                "Invalid part size: part sizes for encrypted multipart uploads must be multiples "
                    + "of the cipher block size ("
                    + blockSize
                    + ") with the exception of the last part.");
        }

        // Generate the envelope symmetric key and initialize a cipher to encrypt the object's data
        MultipartUploadCryptoContext uploadContext = multipartUploadContexts.get(uploadId);
        if (uploadContext == null) {
            throw new Ks3ClientException(
                "No client-side information available on upload ID " + uploadId);
        }

        CipherLite cipherLite = uploadContext.getCipherLite();
        req.setInputStream(newMultipartS3CipherInputStream(req, cipherLite));
        // Treat all encryption requests as input stream upload requests, not as
        // file upload requests.
        req.setFile(null);
        req.setFileoffset(0);
        // The last part of the multipart upload will contain an extra 16-byte mac
        if (req.isLastPart()) {
            // We only change the size of the last part
            req.setPartSize(partSize + (contentCryptoScheme.getTagLengthInBits()/8));
            if (uploadContext.hasFinalPartBeenSeen()) {
                throw new Ks3ClientException(
                    "This part was specified as the last part in a multipart upload, but a previous part was already marked as the last part.  "
                        + "Only the last part of the upload should be marked as the last part.");
            }
            uploadContext.setHasFinalPartBeenSeen(true);
        }

        PartETag result = s3.uploadPart(req);
        return result;
    }

    protected final CipherLiteInputStream newMultipartS3CipherInputStream(
            UploadPartRequest req, CipherLite cipherLite) {
        try {
            InputStream is = req.getInputStream();
            if (req.getFile() != null) {
                is = new InputSubStream(
                    new RepeatableFileInputStream(
                        req.getFile()),
                        req.getFileoffset(), 
                        req.getInstancePartSize(),
                        req.isLastPart());
            }
            return new CipherLiteInputStream(is, cipherLite,
                DEFAULT_BUFFER_SIZE,
                IS_MULTI_PART
            );
        } catch (Exception e) {
            throw new Ks3ClientException(
                    "Unable to create cipher input stream: " + e.getMessage(),
                    e);
        }
    }

    @Override
    public CopyResult copyPartSecurely(CopyPartRequest copyPartRequest) {
        String uploadId = copyPartRequest.getUploadId();
        MultipartUploadCryptoContext uploadContext = multipartUploadContexts.get(uploadId);

        if (!uploadContext.hasFinalPartBeenSeen()) {
            uploadContext.setHasFinalPartBeenSeen(true);
        }

        return s3.copyPart(copyPartRequest);
    }

    /*
     * Private helper methods
     */


    /**
     * Puts an encrypted object into S3, and puts an instruction file into S3. Encryption info is stored in the instruction file.
     *
     * @param putObjectRequest
     *      The request object containing all the parameters to upload a
     *      new object to KS3.
     * @return
     *      A {@link PutObjectResult} object containing the information
     *      returned by KS3 for the new, created object.
     * @throws Ks3ClientException
     *      If any errors are encountered on the client while making the
     *      request or handling the response.
     * @throws Ks3ServiceException
     *      If any errors occurred in S3 while processing the
     *      request.
     */
    private PutObjectResult putObjectUsingInstructionFile(PutObjectRequest putObjectRequest)
            throws Ks3ClientException, Ks3ServiceException {
    	//TODO clone一个新的putObjectRequest给命令文件用？
        // Create instruction
        ContentCryptoMaterial cekMaterial = createContentCryptoMaterial(putObjectRequest);
        // Wraps the object data with a cipher input stream; note the metadata
        // is mutated as a side effect.
        PutObjectRequest req = wrapWithCipher(putObjectRequest, cekMaterial);
        // Put the encrypted object into S3
        PutObjectResult result = s3.putObject(req);
        // Put the instruction file into S3
        s3.putObject(upateInstructionPutRequest(putObjectRequest, cekMaterial));
        // Return the result of the encrypted object PUT.
        return result;
    }

    /**
     * Returns an updated object where the object content input stream contains the decrypted contents.
     *
     * @param wrapper
     *      The object whose contents are to be decrypted.
     * @param cekMaterial
     *      The instruction that will be used to decrypt the object data.
     * @return
     *      The updated object where the object content input stream contains the decrypted contents.
     */
    private S3ObjectWrapper decrypt(S3ObjectWrapper wrapper,
            ContentCryptoMaterial cekMaterial, long[] range) {
        AutoAbortInputStream objectContent = wrapper.getObjectContent();
        wrapper.setObjectContent(new AutoAbortInputStream(
                new CipherLiteInputStream(objectContent, cekMaterial
                        .getCipherLite(), DEFAULT_BUFFER_SIZE), objectContent
                        .getRequest()));
        return wrapper;
    }

    /**
     * Retrieves an instruction file from S3.  If no instruction file is found, returns null.
     *
     * @param getObjectRequest
     *      A GET request for an object in S3.  The parameters from this request will be used
     *      to retrieve the corresponding instruction file.
     * @return
     *      An instruction file, or null if no instruction file was found.
     */
    private S3ObjectWrapper fetchInstructionFile(GetObjectRequest getObjectRequest) {
        try {
            Ks3Object o = s3.getObject(EncryptionUtils.createInstructionGetRequest(getObjectRequest)).getObject();
            return o == null ? null : new S3ObjectWrapper(o);
        } catch (Ks3ServiceException e) {
            // If no instruction file is found, log a debug message, and return null.
            log.debug("Unable to retrieve instruction file : " + e.getMessage());
            return null;
        }
    }

    /**
     * Asserts that the specified parameter value is not null and if it is,
     * throws an IllegalArgumentException with the specified error message.
     *
     * @param parameterValue
     *            The parameter value being checked.
     * @param errorMessage
     *            The error message to include in the IllegalArgumentException
     *            if the specified parameter is null.
     */
    private void assertParameterNotNull(Object parameterValue, String errorMessage) {
        if (parameterValue == null) throw new IllegalArgumentException(errorMessage);
    }

    @Override
    protected final long ciphertextLength(long originalContentLength) {
        // Add 16 bytes for the 128-bit tag length using AES/GCM
        return originalContentLength + contentCryptoScheme.getTagLengthInBits()/8;
    }
}
