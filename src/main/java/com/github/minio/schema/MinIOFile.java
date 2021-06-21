package com.github.minio.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.ZonedDateTime;

/**
 * MinIOFile
 *
 * @author echils
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinIOFile {

    /**
     * filename
     */
    private String filename;

    /**
     * The name of bucket
     */
    private String bucketName;

    /**
     * last modified time
     */
    private ZonedDateTime lastModified;

    /**
     * The size of file
     */
    private long size;

    /**
     * The url of the file,if the bucket is shared the url will never expired,otherwise default 7 day valid time
     */
    private URI uri;

}
