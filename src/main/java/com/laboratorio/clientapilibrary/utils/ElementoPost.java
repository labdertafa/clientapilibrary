package com.laboratorio.clientapilibrary.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 07/08/2024
 * @updated 07/08/2024
 */

@Getter @Setter @AllArgsConstructor
public class ElementoPost {
    private int byteStart;
    private int byteEnd;
    private TipoElementoPost type;
    private String contenido;
}