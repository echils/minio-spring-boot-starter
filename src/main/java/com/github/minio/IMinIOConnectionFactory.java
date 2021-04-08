package com.github.minio;

import io.minio.MinioClient;

/**
 * IMinIOConnectionFactory
 *
 * @author echils
 * @since 2021-04-02 10:27
 */
public interface IMinIOConnectionFactory {


    /**
     * Provides a suitable connection for interacting with minIO.
     *
     * @return connection for interacting with minIO.
     */
    MinioClient getConnection();


}
