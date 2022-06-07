package com.github.minio;

import com.github.minio.schema.MinIOBucket;
import com.github.minio.schema.MinIOFile;
import com.github.minio.schema.MinIOPolicy;
import org.springframework.util.Assert;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * DefaultMinIOTemplate
 *
 * Operate in the default bucket {@link MinIOProperties}
 *
 * @author echils
 */
public class DefaultMinIOTemplate {


    private MinIOTemplate delegate;

    private String defaultBucket;

    public DefaultMinIOTemplate(MinIOTemplate minioTemplate, String defaultBucket) {
        Assert.notNull(minioTemplate, "Client is not allowed empty");
        Assert.notNull(defaultBucket, "Default bucket is not allowed empty");
        this.delegate = minioTemplate;
        this.defaultBucket = defaultBucket;
        if (!delegate.bucketExist(defaultBucket)) delegate.createBucket(defaultBucket);
    }


    /**
     * Get current bucket
     */
    public Optional<MinIOBucket> getCurrentBucket() {
        return delegate.getBucket(defaultBucket);
    }


    /**
     * Config current bucket policy
     *
     * @param policy the policy of bucket
     */
    public void setBucketPolicy(MinIOPolicy.Policy policy) {
        delegate.setBucketPolicy(defaultBucket, policy);
    }


    /**
     * List all files of current bucket
     */
    public List<MinIOFile> listFiles() {
        return delegate.listFiles(defaultBucket);
    }


    /**
     * List current bucket files by prefix
     *
     * @param prefix prefix of the file name
     */
    public List<MinIOFile> listFiles(String prefix) {
        return delegate.listFiles(defaultBucket, prefix);
    }


    /**
     * List current bucket files with filtering
     *
     * @param predicate {@link Predicate}
     */
    public List<MinIOFile> listFiles(Predicate<MinIOFile> predicate) {
        return delegate.listFiles(defaultBucket, predicate);
    }


    /**
     * Get file by filename in current file
     *
     * @param filename the name of file
     */
    public Optional<MinIOFile> getFile(String filename) {
        return delegate.getFile(defaultBucket, filename);
    }


    /**
     * Get url of the file with expired in current file
     *
     * @param filename the name of file
     * @param duration the time of expire
     * @param timeUnit the time of unit
     */
    public URI getFileUrl(String filename, int duration, TimeUnit timeUnit) {
        return delegate.getFileUrl(defaultBucket, filename, duration, timeUnit);
    }


    /**
     * Get the url of the file in current file,if the bucket is shared the url will never expired,otherwise default 7 day valid time
     *
     * @param filename the name of file
     */
    public URI getFileUrl(String filename) {
        return delegate.getFileUrl(defaultBucket, filename);
    }


    /**
     * Copy file in current bucket
     *
     * @param sourceFilename The filename of source bucket
     * @param targetFilename The filename of source file
     */
    public URI copyFile(String sourceFilename, String targetFilename) {
        return delegate.copyFile(defaultBucket, sourceFilename, defaultBucket, targetFilename);
    }


    /**
     * Delete minIo file if exist in current bucket
     *
     * @param filename the name of file
     */
    public void deleteFile(String filename) {
        delegate.deleteFile(defaultBucket, filename);
    }


    /**
     * Batch delete minIo file in current bucket
     *
     * @param filenames the name of files
     */
    public void deleteFiles(List<String> filenames) {
        delegate.deleteFiles(defaultBucket, filenames);
    }


    /**
     * Download file with stream in current bucket
     *
     * @param filename the name of file
     */
    public InputStream downloadFile(String filename) {
        return delegate.downloadFile(defaultBucket, filename);
    }


    /**
     * Download file with path in current bucket
     *
     * @param filename the name of file
     * @param path     the local path of download
     */
    public void downloadFile(String filename, String path) {
        delegate.downloadFile(defaultBucket, filename, path);
    }


    /**
     * Upload file to current bucket by local file,default upload to the bucket path
     *
     * @param file the file of upload
     */
    public URI upload(File file) {
        return delegate.upload(defaultBucket, file);
    }


    /**
     * Upload file to current bucket by file with custom upload path
     *
     * @param uploadPath the path of upload
     * @param file       the file of upload
     */
    public URI upload(String uploadPath, File file) {
        return delegate.upload(defaultBucket, uploadPath, file);
    }


    /**
     * Upload file  to current bucket by inputStream
     *
     * @param uploadPath  the path of upload
     * @param inputStream the stream of upload file
     * @param contentType the content type of upload type
     */
    public URI upload(String uploadPath, InputStream inputStream, String contentType) {
        return delegate.upload(defaultBucket, uploadPath, inputStream, contentType);
    }

}


