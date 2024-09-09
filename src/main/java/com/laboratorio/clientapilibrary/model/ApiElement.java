package com.laboratorio.clientapilibrary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 06/09/2024
 * @updated 08/09/2024
 */

@Getter @Setter @AllArgsConstructor
public class ApiElement {
    private ApiElementType type;
    private String name;
    private ApiValueType valueType;
    private String value;
}