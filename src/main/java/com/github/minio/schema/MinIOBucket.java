package com.github.minio.schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * MinIOBucket
 *
 * @author echils
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinIOBucket {

    /**
     * The name of bucket
     */
    private String name;

    /**
     * The creat time of the bucket
     */
    private ZonedDateTime createTime;
}
