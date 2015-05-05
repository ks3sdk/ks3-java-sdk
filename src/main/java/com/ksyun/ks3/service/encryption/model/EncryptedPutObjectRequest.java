
package com.ksyun.ks3.service.encryption.model;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ksyun.ks3.dto.ObjectMetadata;
import com.ksyun.ks3.service.request.PutObjectRequest;

/**
 * <p>
 * This class is an extension of {@link PutObjectRequest} to allow additional encryption material description
 * to be specified on a per-request basis.In particular, {@link EncryptedPutObjectRequest} is only recognized 
 * by {@link KS3EncryptionJavaClient}.
 * </p>
 * <p>
 * If {@link EncryptedPutObjectRequest} is used against the non-encrypting {@link KS3JavaClient}, the additional 
 * attributes will be ignored.
 * </p>
 */
public class EncryptedPutObjectRequest extends PutObjectRequest implements MaterialsDescriptionProvider {
    
    /**
     * description of encryption materials to be used with this request.
     */
    private Map<String, String> materialsDescription;

    public EncryptedPutObjectRequest(String bucketName, String key, File file) {
        super(bucketName, key, file);
    }

    public EncryptedPutObjectRequest(String bucketName, String key, InputStream input, ObjectMetadata metadata) {
        super(bucketName, key, input, metadata);
    }
    
    public Map<String, String> getMaterialsDescription() {
        return materialsDescription;
    }
    
    /**
     * sets the materials description for the encryption materials to be used with the current PutObjectRequest.
     * @param materialsDescription the materialsDescription to set
     */
    public void setMaterialsDescription(Map<String, String> materialsDescription) {
        this.materialsDescription = materialsDescription == null
                ? null
                : Collections.unmodifiableMap(new HashMap<String,String>(materialsDescription))
                ;
    }
    
    /**
     * sets the materials description for the encryption materials to be used with the current PutObjectRequest.
     * @param materialsDescription the materialsDescription to set
     */
    public EncryptedPutObjectRequest withMaterialsDescription(Map<String, String> materialsDescription) {
        setMaterialsDescription(materialsDescription);
        return this;
    }
    
}
