package com.laboratorio.clientapilibrary.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.1
 * @created 01/09/2024
 * @updated 04/10/2024
 */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SerializableCookie implements Serializable {
    private String name;
    private String value;
    private String domain;
    private int version;
    private String path;
    private String comment;
    private ZonedDateTime expiry;
    
    public String toCookieString() {
        StringBuilder cookieString = new StringBuilder();

        // Obligatorio: nombre y valor
        cookieString.append(name).append("=").append(value).append("; ");

        // Opcionales: dominio, camino, expiración, versión, comentario
        if (domain != null && !domain.isEmpty()) {
            cookieString.append("Domain=").append(domain).append("; ");
        }
        if (path != null && !path.isEmpty()) {
            cookieString.append("Path=").append(path).append("; ");
        }
        if (expiry != null) {
            // Convertir la fecha de expiración al formato GMT (utilizado por Set-Cookie)
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            cookieString.append("Expires=").append(formatter.format(expiry)).append("; ");
        }
        if (version > 0) {
            cookieString.append("Version=").append(version).append("; ");
        }
        if (comment != null && !comment.isEmpty()) {
            cookieString.append("Comment=").append(comment).append("; ");
        }

        // Eliminar el último "; " si existe
        if (cookieString.length() > 2) {
            cookieString.setLength(cookieString.length() - 2); // Eliminar "; "
        }

        return cookieString.toString();
    }
}