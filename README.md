# MinIO Spring Boot Starter Quickstart Guide

This README.md describes how to quickly configure and use the launcher and provides a few common methods as a demonstration, other methods can be seen in the specific code  

<p align="center">
  <a>
   <img alt="Framework" src="ECHILS.PNG">
  </a>
</p>

## Development Environment  
JDK     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1.8.0_202  
Maven   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3.5.4  
Spring Boot &nbsp;&nbsp;&nbsp;&nbsp;2.3.4.RELEASE  
MinIO &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;7.1.4


## Quick Start Example  

##### 1、Add the dependency to the pom.xml  
````
<dependency>
    <groupId>com.github.echils</groupId>
    <artifactId>minio-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
````
##### 2、Add the following configuration to the application.yaml,see the class {@link MinIOProperties} for additional configuration parameters  
````
  spring:
    minio:
      host: localhost
      port: 9000
      username: root
      password: 123456
````

##### 3、Autowired the tool {@link MinIOTemplate} in your service  
````
@Service
public class Test {

    @Autowired
    private MinIOTemplate minIOTemplate;

    private static final String BUCKET_NAME = "Test";
    
    public void invoke(){
    
        List<Bucket> buckets = minIOTemplate.listBuckets();
        
        buckets.forEach(System.out::println);

        if (minIOTemplate.bucketExist(BUCKET_NAME)) {
            minIOTemplate.createBucket(BUCKET_NAME);
        }

        minIOTemplate.upload(BUCKET_NAME, new File("xxxxx"));
        
        ...    
    }
    
}

````
