package com.github.minio;

/**
 * MinIOExecuteException
 *
 * @author echils
 */
public class MinIOExecuteException extends RuntimeException {

    public MinIOExecuteException() {
        super();
    }

    public MinIOExecuteException(String message) {
        super(message);
    }

    public MinIOExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinIOExecuteException(Throwable cause) {
        super(cause);
    }

    protected MinIOExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
