package com.github.minio.schema;

import com.github.minio.MinIOExecuteException;
import org.apache.commons.lang3.StringUtils;

/**
 * MinIOPolicy
 *
 * @author echils
 */
public class MinIOPolicy {

    /**
     * the placeholder of bucket
     */
    private static final String BUCKET_PARAM = "${bucket}";

    /**
     * bucket policy read_only
     */
    private static final String READ_ONLY_JSON = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "/*\"]}]}";

    /**
     * bucket policy write_only
     */
    private static final String WRITE_ONLY_JSON = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:ListMultipartUploadParts\",\"s3:PutObject\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "/*\"]}]}";

    /**
     * bucket policy read_and_write
     */
    private static final String READ_WRITE_JSON = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\",\"s3:PutObject\",\"s3:AbortMultipartUpload\"],\"Resource\":[\"arn:aws:s3:::" + BUCKET_PARAM + "/*\"]}]}";

    /**
     * The name of bucket
     */
    private String bucketName;

    /**
     * Bucket policy
     */
    private Policy policy;


    public MinIOPolicy(String bucketName, Policy policy) {
        this.bucketName = bucketName;
        this.policy = policy;
    }

    /**
     * Determine if it is a shared bucket
     *
     * @param policy the policy of bucket
     */
    public static boolean isShared(String policy) {
        return StringUtils.isNotBlank(policy) &&
                policy.contains("GetBucketLocation") &&
                policy.contains("ListBucketMultipartUploads") &&
                policy.contains("ListBucket") &&
                policy.contains("DeleteObject") &&
                policy.contains("GetObject") &&
                policy.contains("AbortMultipartUpload") &&
                policy.contains("PutObject") &&
                policy.contains("ListMultipartUploadParts");
    }

    /**
     * Apply policy json
     *
     */
    public String apply() {
        String result;
        switch (policy) {
            case READ_ONLY:
                result = READ_ONLY_JSON.replace(BUCKET_PARAM, bucketName);
                break;
            case WRITE_ONLY:
                result = WRITE_ONLY_JSON.replace(BUCKET_PARAM, bucketName);
                break;
            case READ_AND_WRITE:
                result = READ_WRITE_JSON.replace(BUCKET_PARAM, bucketName);
                break;
            default:
                throw new MinIOExecuteException("Not support policy");
        }
        return result;
    }

    /**
     * Bucket policy
     */
    public enum Policy {
        READ_ONLY,
        WRITE_ONLY,
        READ_AND_WRITE
    }

}
