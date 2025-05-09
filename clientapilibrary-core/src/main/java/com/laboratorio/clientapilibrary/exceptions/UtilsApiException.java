package com.laboratorio.clientapilibrary.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 01/08/2024
 * @updated 01/08/2024
 */
public class UtilsApiException extends RuntimeException {
    private static final Logger log = LogManager.getLogger(UtilsApiException.class);
    
    public UtilsApiException(String className, String message) {
        super(message);
        log.error(String.format("Error %s: %s", className, message));
    }
    
    public UtilsApiException(String className, String message, Exception e) {
        super(message);
        log.error(String.format("Error %s: %s", className, message));
        log.error(String.format("Error %s: %s", className, e.getMessage()));
        if (e.getCause() != null) {
            log.error(String.format("Causa %s: %s", className, e.getCause().getMessage()));
        }
    }
}