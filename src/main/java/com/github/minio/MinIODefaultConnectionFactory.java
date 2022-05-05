package com.github.minio;

import io.minio.MinioClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MinIODefaultConnectionFactory
 *
 * @author echils
 */
@Component
public class MinIODefaultConnectionFactory implements IMinIOConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(MinIODefaultConnectionFactory.class);

    @Autowired
    private MinIOProperties minIOProperties;

    @Override
    public MinioClient getConnection() {

        String username = minIOProperties.getUsername();
        String password = minIOProperties.getPassword();
        MinioClient minioClient;
        try {
            if (StringUtils.isNotBlank(minIOProperties.getUrl())) {
                logger.info("MinIO connect with url:{}", minIOProperties.getUrl());
                minioClient = MinioClient.builder().endpoint(minIOProperties.getUrl())
                        .credentials(username, password).build();
            } else {
                String host = minIOProperties.getHost();
                int port = minIOProperties.getPort();
                boolean secure = minIOProperties.isSecure();
                logger.info("MinIO connect with host:{},port:{},secure:{}", host, port, secure);
                minioClient = MinioClient.builder().endpoint(host, port, secure)
                        .credentials(username, password).build();
            }
            minioClient.setTimeout(minIOProperties.getConnectTimeout(), minIOProperties.getWriteTimeout(),
                    minIOProperties.getReadTimeout());
            check(minioClient);
        } catch (Exception e) {
            logger.error("MinIO connect failed:{}", minIOProperties.toString(), e);
            throw new MinIOExecuteException("MinIO connect failed");
        }
        return minioClient;
    }

    /**
     * Test the connection of MinIO
     *
     * @param client {@link MinioClient}
     */
    private void check(MinioClient client) {
        try {
            client.listBuckets();
        } catch (Exception e) {
            throw new MinIOExecuteException(e.getMessage());
        }
    }

}
