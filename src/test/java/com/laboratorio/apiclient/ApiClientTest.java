package com.laboratorio.apiclient;

import com.laboratorio.clientapilibrary.ApiClient;
import com.laboratorio.clientapilibrary.exceptions.ApiClientException;
import com.laboratorio.clientapilibrary.impl.ApiClientImpl;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 06/09/2024
 * @updated 15/09/2024
 */
public class ApiClientTest {
    private static ApiClient client;
    
    @BeforeEach
    public void initTest() {
        client = new ApiClientImpl();
    }
    
    @Test
    public void executeGetRequest() {
        ApiRequest request = new ApiRequest("https://api.gettr.com/s/uinf/labrafa", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        String response = client.executeGetRequest(request);
        
        assertTrue(response.contains("labrafa"));
    }
    
    @Test
    public void executeGetRequestInvalid() {
        ApiRequest request = new ApiRequest("https://api.gettr.com/s/uinf/3423423labrafa/fasdsf", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        assertThrows(ApiClientException.class, () -> {
            client.executeGetRequest(request);
        });
    }
    
    @Test
    public void executeGetRequestBrotliEncoded() {
        String uri = "https://www.minds.com/api/v1/channel/disobedientcitizen";
        ApiRequest request = new ApiRequest(uri, 200);
        
        List<String> cookiesList = client.getWebsiteCookies("https://www.minds.com/labrafa/");
        for (String cookie : cookiesList) {
            request.addApiCookie(cookie);
        }
        
        request.addApiHeader("Accept", "*/*");
        request.addApiHeader("Accept-Encoding", "gzip, deflate, br");
        request.addApiHeader("Connection", "keep-alive");
        request.addApiHeader("X-Version", "1452425022");
        request.addApiHeader("X-Xsrf-Token", "bc062ae65ee5518d1626ed77b9d51b181a5f0cb970bdda26ee78eba8c9015f3773cb80683459d23b65afe15a954a36696377f14edccf6c2132c204a473b7085d");
        
        String response = client.executeGetRequest(request);
        
        assertTrue(response.contains("disobedientcitizen"));
    }
    
    @Test
    public void executePostRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        String response = client.executePostRequest(request);
        
        assertTrue(response != null);
    }
    
    @Test
    public void getResponsePostRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products", 200);
        request.addApiHeader("Content-Type", "application/json");
        request.addTextFormData("Test", "Test");
        
        Response response = client.getResponsePostRequest(request);
        
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void executePostRequestInvalid() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/1", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        assertThrows(ApiClientException.class, () -> {
            client.executePostRequest(request);
        });
    }
    
    @Test
    public void executePutRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/1", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        Response response = client.getResponsePutRequest(request);
        
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void executePutRequestWithFile() {
        String filePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Spa_1950.jpg";
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/1", 200);
        request.setBinaryFile(filePath);
        request.addApiHeader("Content-Type", "application/json");
        
        Response response = client.getResponsePutRequest(request);
        
        assertEquals(200, response.getStatus());
    }
    
    @Test
    public void executePutRequestInvalid() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        assertThrows(ApiClientException.class, () -> {
            client.getResponsePutRequest(request);
        });
    }
    
    @Test
    public void executeDeleteRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/6", 200);
        request.addApiHeader("Content-Type", "application/json");
        
        Response response = client.getResponseDeleteRequest(request);
        
        assertEquals(200, response.getStatus());
    }
}