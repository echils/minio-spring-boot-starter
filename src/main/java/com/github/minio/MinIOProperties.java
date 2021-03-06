package com.github.minio;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIOProperties
 *
 * @author echils
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.minio")
public class MinIOProperties implements InitializingBean {

    /**
     * Connection URL. Overrides host,port and secure.
     * Example: http://localhost:9000
     */
    private String url;

    /**
     * MinIO server host.
     */
    private String host = "localhost";

    /**
     * MinIO server port.
     */
    private int port = 9000;

    /**
     * MinIO server username
     */
    private String username;

    /**
     * MinIO server password
     */
    private String password;

    /**
     * MinIO whether secure
     */
    private boolean secure = false;

    /**
     * MinIO default bucket
     */
    private String defaultBucket = "media";

    /**
     * Define the connect timeout,the unit is millisecond
     */
    private int connectTimeout = 10000;

    /**
     * Define the write timeout,the unit is millisecond
     */
    private int writeTimeout = 60000;

    /**
     * Define the read timeout,the unit is millisecond
     */
    private int readTimeout = 10000;

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isBlank(url) && StringUtils.isBlank(host)) {
            throw new MinIOExecuteException("Connection required parameters cannot be empty");
        }
        if (StringUtils.isBlank(username)) {
            throw new MinIOExecuteException("Username cannot be empty");
        }
        if (StringUtils.isBlank(password)) {
            throw new MinIOExecuteException("Password cannot be empty");
        }
        if (StringUtils.isBlank(defaultBucket)) {
            throw new MinIOExecuteException("Default bucket cannot be empty");
        }
    }

}
