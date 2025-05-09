package com.laboratorio.clientapilibrary.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.1
 * @created 03/10/2024
 * @updated 02/02/2025
 */

@Getter @Setter @AllArgsConstructor
public class ApiResponse {
    private Map<String, List<String>> httpHeaders;
    private List<String> cookies;
    private String responseStr;
    private byte[] responseByte;
}