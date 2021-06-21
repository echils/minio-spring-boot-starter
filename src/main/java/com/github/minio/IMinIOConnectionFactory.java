package com.github.minio;

import io.minio.MinioClient;

/**
 * IMinIOConnectionFactory
 *
 * @author echils
 */
public interface IMinIOConnectionFactory {


    /**
     * Provides a suitable connection for interacting with minIO.
     *
     * @return connection for interacting with minIO.
     */
    MinioClient getConnection();


}
