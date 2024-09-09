package com.laboratorio.clientapilibrary.model;

import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 08/09/2024
 * @updated 08/09/2024
 */

@Getter @Setter @AllArgsConstructor
public class ProcessedResponse {
    private Response response;
    private String responseDetail;
}