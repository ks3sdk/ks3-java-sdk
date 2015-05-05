package com.ksyun.ks3.service.encryption.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contextual information for an in-flight multipart upload.
 */
public abstract class MultipartUploadContext {
    private final String bucketName;
    private final String key;
    private boolean hasFinalPartBeenSeen;
    /** the materialDescription is an optional attribute that is only non-null 
     * when the material description is set on a per request basis 
     */
    private Map<String, String> materialsDescription;

    protected MultipartUploadContext(String bucketName, String key) {
        this.bucketName = bucketName;
        this.key = key;
    }

    public final String getBucketName() {
        return bucketName;
    }

    public final String getKey() {
        return key;
    }

    public final boolean hasFinalPartBeenSeen() {
        return hasFinalPartBeenSeen;
    }

    public final void setHasFinalPartBeenSeen(boolean hasFinalPartBeenSeen) {
        this.hasFinalPartBeenSeen = hasFinalPartBeenSeen;
    }

    /**
     * @return the materialsDescription
     */
    public final Map<String, String> getMaterialsDescription() {
        return materialsDescription;
    }

    /**
     * @param materialsDescription the materialsDescription to set
     */
    public final void setMaterialsDescription(
            Map<String, String> materialsDescription) {
        this.materialsDescription = materialsDescription == null 
            ? null
            : Collections.unmodifiableMap(new HashMap<String, String>(materialsDescription));
    }
}
