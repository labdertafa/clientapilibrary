package com.laboratorio.clientapilibrary.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 06/09/2024
 * @updated 08/09/2024
 */

@Getter @Setter
public class ApiRequest {
    private String uri;
    private int okResponse;
    private List<ApiElement> elements;
    private List<String> cookies;
    private String payload;
    private String binaryFile;
    private boolean formData;

    public ApiRequest(String uri, int okResponse) {
        this.uri = uri;
        this.okResponse = okResponse;
        this.elements = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.payload = null;
        this.binaryFile = null;
        this.formData = false;
    }

    public ApiRequest(String uri, int okResponse, String payload) {
        this.uri = uri;
        this.okResponse = okResponse;
        this.elements = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.payload = payload;
        this.binaryFile = null;
        this.formData = false;
    }
    
    public void addApiPathParam(String name, String value) {
        this.elements.add(new ApiElement(ApiElementType.PATHPARAM, name, ApiValueType.TEXT, value));
    }
    
    public void addApiHeader(String name, String value) {
        this.elements.add(new ApiElement(ApiElementType.HEADER, name, ApiValueType.TEXT, value));
    }
    
    public void addApiCookie(String cookie) {
        this.cookies.add(cookie);
    }
    
    public void addTextFormData(String name, String value) {
       this.formData = true;
       this.elements.add(new ApiElement(ApiElementType.FORMDATA, name, ApiValueType.TEXT, value));
    }
    
    public void addFileFormData(String name, String value) {
       this.formData = true;
       this.elements.add(new ApiElement(ApiElementType.FORMDATA, name, ApiValueType.FILE, value));
    }
    
    public void addJsonFormData(String name, String value) {
       this.formData = true;
       this.elements.add(new ApiElement(ApiElementType.FORMDATA, name, ApiValueType.JSON, value));
    }
}