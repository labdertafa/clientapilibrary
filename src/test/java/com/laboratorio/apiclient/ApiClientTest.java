package com.laboratorio.apiclient;

import com.laboratorio.clientapilibrary.exceptions.ApiClientException;
import com.laboratorio.clientapilibrary.ApiClient;
import com.laboratorio.clientapilibrary.model.ApiMethodType;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import com.laboratorio.clientapilibrary.model.ApiResponse;
import com.laboratorio.clientapilibrary.utils.CookieManager;
import java.io.File;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 06/09/2024
 * @updated 05/10/2024
 */
public class ApiClientTest {
    private static final Logger log = LogManager.getLogger(ApiClientTest.class);
    private static ApiClient client;
    
    @BeforeEach
    public void initTest() {
        client = new ApiClient();
    }
    
    @Test
    public void executeGetRequest() {
        ApiRequest request = new ApiRequest("https://api.gettr.com/s/uinf/labrafa", 200, ApiMethodType.GET);
        request.addApiHeader("Content-Type", "application/json");
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(response.getResponseStr().contains("labrafa"));
    }
    
    @Test
    public void executeGetRequestInvalid() {
        ApiRequest request = new ApiRequest("https://api.gettr.com/s/uinf/3423423labrafa/fasdsf", 200, ApiMethodType.GET);
        request.addApiHeader("Content-Type", "application/json");
        
        assertThrows(ApiClientException.class, () -> {
            client.executeApiRequest(request);
        });
    }
    
    @Test
    public void executeGetRequestBrotliEncoded() {
        String uri = "https://www.minds.com/api/v1/channel/disobedientcitizen";
        ApiRequest request = new ApiRequest(uri, 200, ApiMethodType.GET);
        String userAgent = "PostmanRuntime/7.42.0";
        
        List<String> cookiesList = CookieManager.getWebsiteCookies("temp/cookies.ser", "https://www.minds.com/", userAgent);
        for (String cookie : cookiesList) {
            request.addApiCookie(cookie);
        }
        
        request.addApiHeader("Accept", "*/*");
        request.addApiHeader("Accept-Encoding", "gzip, deflate, br");
        request.addApiHeader("Connection", "keep-alive");
        request.addApiHeader("X-Version", "1452425022");
        request.addApiHeader("X-Xsrf-Token", "bc062ae65ee5518d1626ed77b9d51b181a5f0cb970bdda26ee78eba8c9015f3773cb80683459d23b65afe15a954a36696377f14edccf6c2132c204a473b7085d");
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(response.getResponseStr().contains("disobedientcitizen"));
    }
    
    @Test
    public void executePostRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products", 200, ApiMethodType.POST);
        request.addApiHeader("Content-Type", "application/json");
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(response.getResponseStr() != null);
    }
    
    @Test
    public void getResponsePostRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products", 200, ApiMethodType.POST);
        request.addApiHeader("Content-Type", "application/json");
        request.addTextFormData("Test", "Test");
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(!response.getHttpHeaders().isEmpty());
    }
    
    @Test
    public void executePostRequestInvalid() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/1", 200, ApiMethodType.POST);
        request.addApiHeader("Content-Type", "application/json");
        
        assertThrows(ApiClientException.class, () -> {
            client.executeApiRequest(request);
        });
    }
    
    @Test
    public void executePutRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/1", 200, ApiMethodType.PUT);
        request.addApiHeader("Content-Type", "application/json");
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(!response.getHttpHeaders().isEmpty());
    }
    
    @Test
    public void executePutRequestWithFile() {
        String filePath = "C:\\Users\\rafa\\Pictures\\imagen1.png";
        File file = new File(filePath);
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/1", 200, ApiMethodType.PUT, file);
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(!response.getHttpHeaders().isEmpty());
    }
    
    @Test
    public void executePutRequestInvalid() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products", 200, ApiMethodType.PUT);
        request.addApiHeader("Content-Type", "application/json");
        
        assertThrows(ApiClientException.class, () -> {
            client.executeApiRequest(request);
        });
    }
    
    @Test
    public void executeDeleteRequest() {
        ApiRequest request = new ApiRequest("https://fakestoreapi.com/products/6", 200, ApiMethodType.DELETE);
        request.addApiHeader("Content-Type", "application/json");
        
        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(!response.getHttpHeaders().isEmpty());
    }
    
/*    @Test
    public void renovarTokenImgur() {
        String refreshToken = "c52e7c8f05bd0b30335d31de4558b207b5e92cc6";
        String clientId = "1f3ce0ecda2442e";
        String clientSecret = "121a091ec0350e36f5ca1a72cce5e82f6f619cf7";
        
        ApiRequest request = new ApiRequest("https://api.imgur.com/oauth2/token", 200, ApiMethodType.POST);
        request.addTextFormData("refresh_token", refreshToken);
        request.addTextFormData("client_id", clientId);
        request.addTextFormData("client_secret", clientSecret);
        request.addTextFormData("grant_type", "refresh_token");

        ApiResponse response = client.executeApiRequest(request);
        
        assertTrue(response.getResponseStr().length() > 0);
    } */
}