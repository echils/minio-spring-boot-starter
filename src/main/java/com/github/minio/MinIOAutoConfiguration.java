package com.github.minio;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIOAutoConfiguration
 *
 * @author echils
 */
@Configuration
public class MinIOAutoConfiguration {

    @Bean
    public MinIOProperties minioProperties() {
        return new MinIOProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "connectionFactory")
    public IMinIOConnectionFactory connectionFactory() {
        return new MinIODefaultConnectionFactory();
    }

    @Bean
    @ConditionalOnMissingBean(name = "minIOTemplate")
    public MinIOTemplate minIOTemplate(IMinIOConnectionFactory minIOConnectionFactory) {
        return new MinIOTemplate(minIOConnectionFactory.getConnection());
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultMinIOTemplate")
    public DefaultMinIOTemplate defaultMinIOTemplate(IMinIOConnectionFactory connectionFactory,
                                                     MinIOProperties minioProperties) {

        return new DefaultMinIOTemplate(new MinIOTemplate(connectionFactory.getConnection()),
                minioProperties.getDefaultBucket());
    }

}
