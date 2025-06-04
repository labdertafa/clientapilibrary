package com.laboratorio.clientapilibrary.exceptions;

/**
 *
 * @author Rafael
 * @version 1.1
 * @created 10/07/2024
 * @updated 02/05/2025
 */
public class ApiClientException extends RuntimeException {
    private Throwable causaOriginal = null;
    
    public ApiClientException(String message, Throwable causaOriginal) {
        super(message, causaOriginal);
        this.causaOriginal = causaOriginal;
    }

    public ApiClientException(String message) {
        super(message);
    }
    
    @Override
    public String getMessage() {
        if (this.causaOriginal != null) {
            return super.getMessage() + " | Causa original: " + this.causaOriginal.getMessage();
        }
        
        return super.getMessage();
    }
}