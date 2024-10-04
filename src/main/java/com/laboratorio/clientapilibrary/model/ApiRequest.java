package com.laboratorio.clientapilibrary.model;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Rafael
 * @version 2.0
 * @created 06/09/2024
 * @updated 04/10/2024
 */

@Getter @Setter
public class ApiRequest {
    private final String uri;
    private final int okResponse;
    private final ApiMethodType method;
    private List<ApiElement> elements;
    private List<String> cookies;
    private String payload;
    private File binaryFile;
    private boolean formData;

    public ApiRequest(String uri, int okResponse, ApiMethodType method) {
        this.uri = uri;
        this.okResponse = okResponse;
        this.method = method;
        this.elements = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.payload = null;
        this.binaryFile = null;
        this.formData = false;
    }
    
    public ApiRequest(String uri, int okResponse, ApiMethodType method, String payload) {
        this.uri = uri;
        this.okResponse = okResponse;
        this.method = method;
        this.elements = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.payload = payload;
        this.binaryFile = null;
        this.formData = false;
    }
    
    public ApiRequest(String uri, int okResponse, ApiMethodType method, File binaryFile) {
        this.uri = uri;
        this.okResponse = okResponse;
        this.method = method;
        this.elements = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.payload = null;
        this.binaryFile = binaryFile;
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
    
    public String getQueryParams() {
        StringBuilder queryParam = null;
        
        for (ApiElement element : this.elements) {
            if (element.getType() == ApiElementType.PATHPARAM) {
                if (queryParam == null) {
                    queryParam = new StringBuilder("?");
                } else {
                    queryParam.append("&");
                }
                queryParam.append(element.getName()).append("=");
                queryParam.append(URLEncoder.encode(element.getValue(), StandardCharsets.UTF_8));
            }
        }
        
        if (queryParam == null) {
            return "";
        }
        
        return queryParam.toString();
    }
}