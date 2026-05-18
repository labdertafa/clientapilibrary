package com.laboratorio.clientapilibrary.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.1
 * @created 07/09/2024
 * @updated 18/05/2026
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ImageMetadata {
    private String filename;
    private int width;
    private int height;
    private String mimeType;
    private int size;

    @Override
    public String toString() {
        return "ImageMetadata{" + "filename=" + filename +", width="+ width + ", height=" +
                height + ", mimeType=" + mimeType + ", size=" + size + '}';
    }
}