
package com.ksyun.ks3.service.encryption.internal;


import java.io.File;



import javax.crypto.Cipher;
import javax.crypto.SecretKey;

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
import com.ksyun.ks3.service.encryption.model.EncryptedInitiateMultipartUploadRequest;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterials;
import com.ksyun.ks3.service.encryption.model.EncryptionMaterialsProvider;
import com.ksyun.ks3.service.encryption.model.MaterialsDescriptionProvider;
import com.ksyun.ks3.service.request.CompleteMultipartUploadRequest;
import com.ksyun.ks3.service.request.CopyPartRequest;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ksyun.ks3.service.request.InitiateMultipartUploadRequest;
import com.ksyun.ks3.service.request.Ks3WebServiceRequest;
import com.ksyun.ks3.service.request.PutObjectRequest;
import com.ksyun.ks3.service.request.UploadPartRequest;

import static com.ksyun.ks3.service.encryption.internal.EncryptionUtils.*;


/**
 * Encryption only (EO) cryptographic module for the S3 encryption client.
 */
class S3CryptoModuleEO extends S3CryptoModuleBase<EncryptedUploadContext> {
    S3CryptoModuleEO(S3Direct s3,
            EncryptionMaterialsProvider encryptionMaterialsProvider,
            CryptoConfiguration cryptoConfig) {
        super(s3, encryptionMaterialsProvider,
               cryptoConfig,
                new S3CryptoScheme(ContentCryptoScheme.AES_CBC));
    }


    @Override
    public PutObjectResult putObjectSecurely(PutObjectRequest putObjectRequest)
            throws Ks3ClientException,Ks3ServiceException {
        appendUserAgent(putObjectRequest,Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);

        if (this.cryptoConfig.getStorageMode() == CryptoStorageMode.InstructionFile) {
            return putObjectUsingInstructionFile(putObjectRequest);
        } else {
            return putObjectUsingMetadata(putObjectRequest);
        }
    }

    @Override
    public GetObjectResult getObjectSecurely(GetObjectRequest getObjectRequest)
            throws Ks3ClientException,Ks3ServiceException {
        // Should never get here, as S3 object encrypted in either EO or AE
        // format should all be handled by the AE module.
        throw new IllegalStateException();
    }

    @Override
    public ObjectMetadata getObjectSecurely(GetObjectRequest getObjectRequest, File destinationFile)
            throws Ks3ClientException,Ks3ServiceException {
        // Should never get here, as S3 object encrypted in either EO or AE
        // format should all be handled by the AE module.
        throw new IllegalStateException();
    }


    @Override
    public CompleteMultipartUploadResult completeMultipartUploadSecurely(
            CompleteMultipartUploadRequest completeMultipartUploadRequest)
                    throws Ks3ClientException,Ks3ServiceException {
        appendUserAgent(completeMultipartUploadRequest, Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);

        String uploadId = completeMultipartUploadRequest.getUploadId();
        EncryptedUploadContext encryptedUploadContext = multipartUploadContexts.get(uploadId);

        if (encryptedUploadContext.hasFinalPartBeenSeen() == false) {
            throw new Ks3ClientException("Unable to complete an encrypted multipart upload without being told which part was the last.  " +
                    "Without knowing which part was the last, the encrypted data in KS3 is incomplete and corrupt.");
        }

        CompleteMultipartUploadResult result = s3.completeMultipartUpload(completeMultipartUploadRequest);

        // In InstructionFile mode, we want to write the instruction file only after the whole upload has completed correctly.
        if (cryptoConfig.getStorageMode() == CryptoStorageMode.InstructionFile) {
            Cipher symmetricCipher = createSymmetricCipher(
                    encryptedUploadContext.getEnvelopeEncryptionKey(),
                    Cipher.ENCRYPT_MODE, cryptoConfig.getCryptoProvider(),
                    encryptedUploadContext.getFirstInitializationVector());

            EncryptionMaterials encryptionMaterials;
            if (encryptedUploadContext.getMaterialsDescription() != null) {
                encryptionMaterials = kekMaterialsProvider.getEncryptionMaterials(encryptedUploadContext.getMaterialsDescription());
            } else {
                encryptionMaterials = kekMaterialsProvider.getEncryptionMaterials();
            }

            // Encrypt the envelope symmetric key
            byte[] encryptedEnvelopeSymmetricKey = getEncryptedSymmetricKey(encryptedUploadContext.getEnvelopeEncryptionKey(), encryptionMaterials, cryptoConfig.getCryptoProvider());
            EncryptionInstruction instruction = new EncryptionInstruction(encryptionMaterials.getMaterialsDescription(), encryptedEnvelopeSymmetricKey, encryptedUploadContext.getEnvelopeEncryptionKey(), symmetricCipher);

            // Put the instruction file into S3
            s3.putObject(EncryptionUtils.createInstructionPutRequest(encryptedUploadContext.getBucketName(), encryptedUploadContext.getKey(), instruction));
        }

        multipartUploadContexts.remove(uploadId);
        return result;
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUploadSecurely(
            InitiateMultipartUploadRequest initiateMultipartUploadRequest)
                    throws Ks3ClientException, Ks3ServiceException {
        appendUserAgent(initiateMultipartUploadRequest, Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);

        // Generate a one-time use symmetric key and initialize a cipher to encrypt object data
        SecretKey envelopeSymmetricKey = generateOneTimeUseSymmetricKey();
        Cipher symmetricCipher = createSymmetricCipher(envelopeSymmetricKey, Cipher.ENCRYPT_MODE, cryptoConfig.getCryptoProvider(), null);

        if (cryptoConfig.getStorageMode() == CryptoStorageMode.ObjectMetadata) {
            EncryptionMaterials encryptionMaterials = null;
            if (initiateMultipartUploadRequest instanceof EncryptedInitiateMultipartUploadRequest) {
                encryptionMaterials = kekMaterialsProvider.getEncryptionMaterials(((EncryptedInitiateMultipartUploadRequest) initiateMultipartUploadRequest).getMaterialsDescription());
            } else {
                encryptionMaterials = kekMaterialsProvider.getEncryptionMaterials();
            }
            // Encrypt the envelope symmetric key
            byte[] encryptedEnvelopeSymmetricKey = getEncryptedSymmetricKey(envelopeSymmetricKey, encryptionMaterials, cryptoConfig.getCryptoProvider());

            // Store encryption info in metadata
            ObjectMetadata metadata = EncryptionUtils.updateMetadataWithEncryptionInfo(initiateMultipartUploadRequest, encryptedEnvelopeSymmetricKey, symmetricCipher, encryptionMaterials.getMaterialsDescription());

            // Update the request's metadata to the updated metadata
            initiateMultipartUploadRequest.setObjectMeta(metadata);
        }

        InitiateMultipartUploadResult result = s3.initiateMultipartUpload(initiateMultipartUploadRequest);
        EncryptedUploadContext encryptedUploadContext = new EncryptedUploadContext(initiateMultipartUploadRequest.getBucket(), initiateMultipartUploadRequest.getKey(), envelopeSymmetricKey);
        encryptedUploadContext.setNextInitializationVector(symmetricCipher.getIV());
        encryptedUploadContext.setFirstInitializationVector(symmetricCipher.getIV());
        if (initiateMultipartUploadRequest instanceof EncryptedInitiateMultipartUploadRequest) {
            encryptedUploadContext.setMaterialsDescription(((EncryptedInitiateMultipartUploadRequest) initiateMultipartUploadRequest).getMaterialsDescription());
        }
        multipartUploadContexts.put(result.getUploadId(), encryptedUploadContext);

        return result;
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
    public PartETag uploadPartSecurely(UploadPartRequest uploadPartRequest)
        throws Ks3ClientException, Ks3ServiceException {

        appendUserAgent(uploadPartRequest, Constants.KS3_ENCRYPTION_CLIENT_USER_AGENT);

        boolean isLastPart = uploadPartRequest.isLastPart();
        String uploadId = uploadPartRequest.getUploadId();

        boolean partSizeMultipleOfCipherBlockSize = uploadPartRequest.getInstancePartSize() % JceEncryptionConstants.SYMMETRIC_CIPHER_BLOCK_SIZE == 0;
        if (!isLastPart && !partSizeMultipleOfCipherBlockSize) {
            throw new Ks3ClientException("Invalid part size: part sizes for encrypted multipart uploads must be multiples " +
                    "of the cipher block size (" + JceEncryptionConstants.SYMMETRIC_CIPHER_BLOCK_SIZE + ") with the exception of the last part.  " +
                    "Otherwise encryption adds extra padding that will corrupt the final object.");
        }

        // Generate the envelope symmetric key and initialize a cipher to encrypt the object's data
        EncryptedUploadContext encryptedUploadContext = multipartUploadContexts.get(uploadId);
        if (encryptedUploadContext == null) throw new Ks3ClientException("No client-side information available on upload ID " + uploadId);

        SecretKey envelopeSymmetricKey = encryptedUploadContext.getEnvelopeEncryptionKey();
        byte[] iv = encryptedUploadContext.getNextInitializationVector();
        CipherFactory cipherFactory = new CipherFactory(envelopeSymmetricKey, Cipher.ENCRYPT_MODE, iv, this.cryptoConfig.getCryptoProvider());

        // Create encrypted input stream
        ByteRangeCapturingInputStream encryptedInputStream = EncryptionUtils.getEncryptedInputStream(uploadPartRequest, cipherFactory);
        uploadPartRequest.setInputStream(encryptedInputStream);

        // The last part of the multipart upload will contain extra padding from the encryption process
        if (uploadPartRequest.isLastPart()) {
            // We only change the size of the last part
            long cryptoContentLength = EncryptionUtils.calculateCryptoContentLength(cipherFactory.createCipher(), uploadPartRequest);
            if (cryptoContentLength > 0) uploadPartRequest.setPartSize(cryptoContentLength);

            if (encryptedUploadContext.hasFinalPartBeenSeen()) {
                throw new Ks3ClientException("This part was specified as the last part in a multipart upload, but a previous part was already marked as the last part.  " +
                        "Only the last part of the upload should be marked as the last part, otherwise it will cause the encrypted data to be corrupted.");
            }

            encryptedUploadContext.setHasFinalPartBeenSeen(true);
        }

        // Treat all encryption requests as input stream upload requests, not as file upload requests.
        uploadPartRequest.setFile(null);
        uploadPartRequest.setFileoffset(0);

        PartETag result = s3.uploadPart(uploadPartRequest);
        encryptedUploadContext.setNextInitializationVector(encryptedInputStream.getBlock());
        return result;
    }

    @Override
    public CopyResult copyPartSecurely(CopyPartRequest copyPartRequest) {
        String uploadId = copyPartRequest.getUploadId();
        EncryptedUploadContext encryptedUploadContext = multipartUploadContexts.get(uploadId);

        if (!encryptedUploadContext.hasFinalPartBeenSeen()) {
            encryptedUploadContext.setHasFinalPartBeenSeen(true);
        }

        return s3.copyPart(copyPartRequest);
    }

    /*
     * Private helper methods
     */

    /**
     * Puts an encrypted object into S3 and stores encryption info in the object metadata.
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
     *      If any errors occurred in KS3 while processing the
     *      request.
     */
    private PutObjectResult putObjectUsingMetadata(PutObjectRequest putObjectRequest)
            throws Ks3ClientException, Ks3ServiceException {
        // Create instruction
        EncryptionInstruction instruction = encryptionInstructionOf(putObjectRequest);
        
        // Encrypt the object data with the instruction
        PutObjectRequest encryptedObjectRequest = encryptRequestUsingInstruction(putObjectRequest, instruction);

        // Update the metadata
        EncryptionUtils.updateMetadataWithEncryptionInstruction( putObjectRequest, instruction );

        // Put the encrypted object into S3
        return s3.putObject(encryptedObjectRequest);
    }

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
     *      If any errors occurred in KS3 while processing the
     *      request.
     */
    private PutObjectResult putObjectUsingInstructionFile(PutObjectRequest putObjectRequest)
            throws Ks3ClientException, Ks3ServiceException {
        // Create instruction
        EncryptionInstruction instruction = encryptionInstructionOf(putObjectRequest);
        
        // Encrypt the object data with the instruction
        PutObjectRequest encryptedObjectRequest = encryptRequestUsingInstruction(putObjectRequest, instruction);

        // Put the encrypted object into S3
        PutObjectResult encryptedObjectResult = s3.putObject(encryptedObjectRequest);

        // Put the instruction file into S3
        PutObjectRequest instructionRequest = EncryptionUtils.createInstructionPutRequest(putObjectRequest, instruction);
        s3.putObject(instructionRequest);

        // Return the result of the encrypted object PUT.
        return encryptedObjectResult;
    }
    
    private EncryptionInstruction encryptionInstructionOf(
            Ks3WebServiceRequest req) {
        EncryptionInstruction instruction;
        if (req instanceof MaterialsDescriptionProvider) {
            MaterialsDescriptionProvider p = (MaterialsDescriptionProvider)req;
            instruction = generateInstruction(this.kekMaterialsProvider,
                    p.getMaterialsDescription(),
                    this.cryptoConfig.getCryptoProvider());
        } else {
            instruction = generateInstruction(this.kekMaterialsProvider, this.cryptoConfig.getCryptoProvider());
        }
        return instruction;
    }

    @Override
    protected final long ciphertextLength(long plaintextLength) {
        long cipherBlockSize = contentCryptoScheme.getBlockSizeInBytes();
        long offset = cipherBlockSize - (plaintextLength % cipherBlockSize);
        return plaintextLength + offset;
    }
}
