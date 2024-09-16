package com.laboratorio.clientapilibrary;

import com.laboratorio.clientapilibrary.exceptions.ApiClientException;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import com.laboratorio.clientapilibrary.model.ProcessedResponse;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 06/09/2024
 * @updated 16/09/2024
 */
public interface ApiClient {
    List<String> getWebsiteCookies(String uri);
    
    String executeGetRequest(ApiRequest request) throws ApiClientException;
    Response getResponseGetRequest(ApiRequest request) throws ApiClientException;
    ProcessedResponse getProcessedResponseGetRequest(ApiRequest request) throws ApiClientException;
    
    String executePostRequest(ApiRequest request) throws ApiClientException;
    Response getResponsePostRequest(ApiRequest request) throws ApiClientException;
    
    Response getResponsePutRequest(ApiRequest request) throws ApiClientException;
    
    String executeDeleteRequest(ApiRequest request);
    Response getResponseDeleteRequest(ApiRequest request) throws ApiClientException;
    ProcessedResponse getProcessedResponseDeleteRequest(ApiRequest request) throws ApiClientException;
}