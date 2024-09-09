package com.laboratorio.clientapilibrary.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 07/09/2024
 * @updated 09/09/2024
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ImageMetadata {
    private int width;
    private int height;
    private String mimeType;
    private int size;
}