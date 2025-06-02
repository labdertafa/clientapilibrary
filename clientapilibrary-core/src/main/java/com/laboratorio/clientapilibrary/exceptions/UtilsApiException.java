package com.laboratorio.clientapilibrary.exceptions;

/**
 *
 * @author Rafael
 * @version 1.1
 * @created 01/08/2024
 * @updated 02/05/2025
 */
public class UtilsApiException extends RuntimeException {
    private Throwable causaOriginal = null;

    public UtilsApiException(String message, Throwable causaOriginal) {
        super(message);
        this.causaOriginal = causaOriginal;
    }
    
    public UtilsApiException(String message) {
        super(message);
    }
    
    public String gerMessage() {
        if (this.causaOriginal != null) {
            return super.getMessage() + " | Causa original: " + this.causaOriginal.getMessage();
        }
        
        return super.getMessage();
    }
}