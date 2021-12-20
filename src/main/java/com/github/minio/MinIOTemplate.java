package com.github.minio;

import com.github.minio.schema.MinIOFile;
import com.github.minio.schema.MinIOPolicy;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.minio.schema.MinIOContentType.getContentType;
import static com.github.minio.schema.MinIOPolicy.isShared;

/**
 * MinIOTemplate
 *
 * @author echils
 */
public class MinIOTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MinIOTemplate.class);

    private static final String SEPARATOR_REGULAR = "/".equals(File.separator) ? File.separator : "\\\\";

    private MinioClient minioClient;

    public MinIOTemplate(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    /**
     * List all the buckets
     *
     */
    public List<Bucket> listBuckets() {
        return listBuckets(null);
    }


    /**
     * List all the buckets and support filtering
     *
     * @param predicate {@link Predicate}
     */
    public List<Bucket> listBuckets(Predicate<Bucket> predicate) {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            if (CollectionUtils.isEmpty(buckets)) {
                return Collections.emptyList();
            }
            if (predicate != null) {
                buckets = buckets.stream().filter(predicate).collect(Collectors.toList());
            }
            return buckets;
        } catch (Exception e) {
            logger.error("MinIO list buckets failed:{}", e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Get bucket by name
     *
     * @param bucketName the name of bucket
     */
    public Optional<Bucket> getBucket(String bucketName) {
        return listBuckets().stream().filter(bucket -> bucket.name().equals(bucketName)).findFirst();
    }


    /**
     * Determine whether the bucket exists
     *
     * @param bucketName the name of bucket
     */
    public boolean bucketExist(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            logger.error("MinIO determine whether the bucket:{} exists failed:{}", bucketName, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Create a bucket with default policy
     *
     * @param bucketName the name of bucket
     */
    public void createBucket(String bucketName) {
        this.createBucket(bucketName, MinIOPolicy.Policy.READ_AND_WRITE);
    }


    /**
     * Create a bucket
     *
     * @param bucketName the name of bucket
     * @param policy     the policy of bucket
     */
    public void createBucket(String bucketName, MinIOPolicy.Policy policy) {
        if (bucketExist(bucketName)) {
            throw new MinIOExecuteException("A bucket of the same name already exists：" + bucketName);
        }
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            String config = new MinIOPolicy(bucketName, policy).apply();
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(config).build());
        } catch (Exception e) {
            logger.error("MinIO create bucket:{} failed:{}", bucketName, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Config bucket policy
     *
     * @param bucketName the name of bucket
     * @param policy     the policy of bucket
     */
    public void setBucketPolicy(String bucketName, MinIOPolicy.Policy policy) {
        if (bucketExist(bucketName)) {
            try {
                String config = new MinIOPolicy(bucketName, policy).apply();
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(config).build());
            } catch (Exception e) {
                logger.error("MinIO set policy of bucket:{} failed:{}", bucketName, e.getMessage());
                throw new MinIOExecuteException(e);
            }
        }
    }


    /**
     * Delete bucket and all file in this bucket
     *
     * @param bucketName the name of bucket
     */
    public void deleteBucket(String bucketName) {
        if (bucketExist(bucketName)) {
            List<MinIOFile> minIOFiles = listFiles(bucketName);
            if (!CollectionUtils.isEmpty(minIOFiles)) {
                deleteFiles(bucketName, minIOFiles.stream().map(MinIOFile::getFilename).collect(Collectors.toList()));
            }
            try {
                minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            } catch (Exception e) {
                logger.error("MinIO delete bucket:{} failed:{}", bucketName, e.getMessage());
                throw new MinIOExecuteException(e);
            }
        }
    }


    /**
     * List all files of bucket
     *
     * @param bucketName the name of bucket
     */
    public List<MinIOFile> listFiles(String bucketName) {
        if (!bucketExist(bucketName)) {
            return Collections.emptyList();
        }
        List<MinIOFile> minIOFiles = new ArrayList<>();
        minioClient.listObjects(ListObjectsArgs.builder().recursive(true)
                .bucket(bucketName).build()).forEach(itemResult -> {
            try {
                Item item = itemResult.get();
                MinIOFile minioFile = new MinIOFile(item.objectName(), bucketName,
                        item.lastModified(), item.size(), getFileUrl(bucketName, item.objectName()));
                minIOFiles.add(minioFile);
            } catch (Exception e) {
                logger.error("MinIO list files of bucket:{} failed:{}", bucketName, e.getMessage());
                throw new MinIOExecuteException(e);
            }
        });
        return minIOFiles;
    }


    /**
     * List all files by bucketName and prefix
     *
     * @param bucketName the name of bucket
     * @param prefix     prefix of the file name
     */
    public List<MinIOFile> listFiles(String bucketName, String prefix) {
        if (!bucketExist(bucketName)) {
            return Collections.emptyList();
        }
        List<MinIOFile> minIOFiles = new ArrayList<>();
        minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).recursive(true)
                .prefix(prefix).build()).forEach(itemResult -> {
            try {
                Item item = itemResult.get();
                MinIOFile minioFile = new MinIOFile(item.objectName(), bucketName,
                        item.lastModified(), item.size(), getFileUrl(bucketName, item.objectName()));
                minIOFiles.add(minioFile);
            } catch (Exception e) {
                logger.error("MinIO list files of bucket:{} and prefix:{} failed:{}", bucketName, prefix, e.getMessage());
                throw new MinIOExecuteException(e);
            }
        });
        return minIOFiles;
    }


    /**
     * List all files by bucketName and support filtering
     *
     * @param bucketName the name of bucket
     * @param predicate  {@link Predicate}
     */
    public List<MinIOFile> listFiles(String bucketName, Predicate<MinIOFile> predicate) {
        List<MinIOFile> minIOFiles = listFiles(bucketName);
        if (predicate != null) {
            return minIOFiles.stream().filter(predicate).collect(Collectors.toList());
        }
        return minIOFiles;
    }


    /**
     * Get minio file by filename
     *
     * @param bucketName the name of bucket
     * @param filename   the name of file
     */
    public Optional<MinIOFile> getFile(String bucketName, String filename) {
        if (!bucketExist(bucketName)) {
            return Optional.empty();
        }
        if (filename.startsWith("/")) {
            filename = filename.substring(filename.indexOf("/") + 1);
        }
        for (Result<Item> itemResult : minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName).recursive(true).prefix(filename).build())) {
            try {
                Item item = itemResult.get();
                if (item.objectName().trim().equals(filename.trim())) {
                    return Optional.of(new MinIOFile(item.objectName(), bucketName,
                            item.lastModified(), item.size(), getFileUrl(bucketName, filename)));
                }
            } catch (Exception e) {
                logger.error("MinIO get file of bucket name:{} and filename:{} failed:{}", bucketName, filename, e.getMessage());
                throw new MinIOExecuteException(e);
            }
        }
        return Optional.empty();
    }


    /**
     * Get url of the file with expired
     *
     * @param bucketName the name of bucket
     * @param filename   the name of file
     * @param duration   the time of expire
     * @param timeUnit   the time of unit
     */
    public URI getFileUrl(String bucketName, String filename, int duration, TimeUnit timeUnit) {
        try {
            if (filename.startsWith("/")) {
                filename = filename.substring(filename.indexOf("/") + 1);
            }
            String bucketPolicy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).bucket(bucketName).object(filename).expiry(duration, timeUnit).build());
            if (isShared(bucketPolicy)) {
                url = url.substring(0, url.indexOf("?"));
            }
            return new URI(url);
        } catch (Exception e) {
            logger.error("MinIO get url of bucket name:{} and filename:{} failed:{}", bucketName, filename, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Get the url of the file,if the bucket is shared the url will never expired,otherwise default 7 day valid time
     *
     * @param bucketName the name of bucket
     * @param filename   the name of file
     */
    public URI getFileUrl(String bucketName, String filename) {
        return getFileUrl(bucketName, filename, 7, TimeUnit.DAYS);
    }


    /**
     * Copy file of bucket
     *
     * @param sourceBucketName The name of source bucket
     * @param sourceFilename   The filename of source bucket
     * @param targetBucketName The name of target bucket
     * @param targetFilename   The filename of source file
     */
    public URI copyFile(String sourceBucketName, String sourceFilename, String targetBucketName, String targetFilename) {
        Optional<MinIOFile> sourceFile = getFile(sourceBucketName, sourceFilename);
        if (!sourceFile.isPresent()) {
            logger.error("No such file where bucket:{},filename:{}", sourceBucketName, sourceFile);
            throw new MinIOExecuteException("No such file");
        }
        if (!bucketExist(targetBucketName)) {
            createBucket(targetBucketName);
        }
        try {
            CopySource source = CopySource.builder().bucket(sourceBucketName).object(sourceFilename).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(targetBucketName).object(targetFilename).source(source).build());
            return getFileUrl(targetBucketName, targetFilename);
        } catch (Exception e) {
            logger.error("MinIO copy file of bucket name:{} and filename:{} failed:{}", sourceBucketName, sourceFilename, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Delete minio file if exist
     *
     * @param bucketName the name of bucket
     * @param filename   the name of file
     */
    public void deleteFile(String bucketName, String filename) {
        if (bucketExist(bucketName)) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build());
            } catch (Exception e) {
                logger.error("MinIO delete file of bucket name:{} and filename:{} failed:{}", bucketName, filename, e.getMessage());
                throw new MinIOExecuteException(e);
            }
        }
    }


    /**
     * Batch delete minio file
     * warn: minioClient.removeObjects() is not work
     *
     * @param bucketName the name of bucket
     * @param filenames  the name of files
     */
    public void deleteFiles(String bucketName, List<String> filenames) {
        if (bucketExist(bucketName)) {
            try {
                if (!CollectionUtils.isEmpty(filenames)) {
                    filenames.forEach(name -> deleteFile(bucketName, name));
                }
            } catch (Exception e) {
                logger.error("MinIO batch delete files failed:{}", e.getMessage());
                throw new MinIOExecuteException(e);
            }
        }
    }


    /**
     * Download file with stream
     *
     * @param bucketName the name of bucket
     * @param filename   the name of file
     */
    public InputStream downloadFile(String bucketName, String filename) {
        Optional<MinIOFile> fileOptional = getFile(bucketName, filename);
        if (!fileOptional.isPresent()) {
            throw new MinIOExecuteException("No such file");
        }
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(filename).build());
        } catch (Exception e) {
            logger.error("MinIO download file of bucket name:{} and filename:{} failed:{}", bucketName, filename, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Download file with path
     *
     * @param bucketName the name of bucket
     * @param filename   the name of file
     * @param path       the local path of download
     */
    public void downloadFile(String bucketName, String filename, String path) {
        Optional<MinIOFile> fileOptional = getFile(bucketName, filename);
        if (!fileOptional.isPresent()) {
            logger.error("No such file where bucket name:{} and filename:{}", bucketName, filename);
            throw new MinIOExecuteException("No such file");
        }

        if (StringUtils.isEmpty(path)) {
            throw new MinIOExecuteException("Illegal file path");
        }

        File file = new File(path);
        if (file.isDirectory()) {
            throw new MinIOExecuteException("The path is directory");
        }

        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        try {
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucketName).object(filename).filename(path).build());
        } catch (Exception e) {
            logger.error("MinIO download file of bucket name:{} and filename:{} failed:{}", bucketName, filename, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Upload file by file,default upload to the bucket path
     *
     * @param bucketName the name of bucket
     * @param file       the file of upload
     */
    public URI upload(String bucketName, File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return upload(bucketName, file.getName(), fileInputStream, getContentType(file));
        } catch (FileNotFoundException e) {
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Upload file by file with custom upload path
     *
     * @param bucketName the name of bucket
     * @param uploadPath the path of upload
     * @param file       the file of upload
     */
    public URI upload(String bucketName, String uploadPath, File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return upload(bucketName, uploadPath, fileInputStream, getContentType(file));
        } catch (FileNotFoundException e) {
            throw new MinIOExecuteException(e);
        }
    }


    /**
     * Upload file by inputStream
     *
     * @param bucketName  the name of bucket
     * @param uploadPath  the path of upload
     * @param inputStream the stream of upload file
     * @param contentType the content type of upload type
     */
    public URI upload(String bucketName, String uploadPath, InputStream inputStream, String contentType) {
        if (!bucketExist(bucketName)) {
            createBucket(bucketName);
        }

        if (StringUtils.isEmpty(uploadPath)) {
            throw new MinIOExecuteException("Illegal upload path");
        }

        uploadPath = uploadPath.replaceAll(SEPARATOR_REGULAR, "/");
        if (uploadPath.startsWith("/")) {
            uploadPath = uploadPath.substring(uploadPath.indexOf("/") + 1);
        }

        if (!uploadPath.contains(".")) {
            throw new MinIOExecuteException("Illegal upload path，missing file type");
        }

        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(uploadPath).stream(
                    inputStream, inputStream.available(), -1)
                    .contentType(contentType)
                    .build());

            return getFileUrl(bucketName, uploadPath);
        } catch (Exception e) {
            logger.error("MinIO upload file of bucket name:{} and upload path:{} failed:{}", bucketName, uploadPath, e.getMessage());
            throw new MinIOExecuteException(e);
        }
    }

}


