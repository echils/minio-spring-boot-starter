package com.github.minio.schema;

import org.apache.commons.lang3.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * MinIOContentType
 *
 * @author echils
 * @since 2021-04-06 16:13
 */
public class MinIOContentType {

    /**
     * Default content type
     */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * Get content type of file
     *
     * @param file the upload file
     * @return
     */
    public static String getContentType(File file) {
        String contentType = null;
        if (file != null) {
            Path path = Paths.get(file.toURI());
            try {
                contentType = Files.probeContentType(path);
            } catch (IOException ignored) {
            }
            if (StringUtils.isBlank(contentType)) {
                contentType = new MimetypesFileTypeMap().getContentType(file);
            }
            contentType = StringUtils.isBlank(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
        }
        return contentType;
    }

}
