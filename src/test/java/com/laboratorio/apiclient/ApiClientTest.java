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
    
    /* @Test
    public void CargarImagenImgur() {
        String accessToken = "f93ef05502cec22d885640502cf513eea7c5f0d0";
        String imagePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Monza_1955.jpg";
        String title = "Título de prueba";
        String description = "Descripción de prueba";
        
        ApiRequest request = new ApiRequest("https://api.imgur.com/3/image", 200, ApiMethodType.POST);
        request.addFileFormData("image", imagePath);
        request.addTextFormData("type", "image");
        request.addTextFormData("title", title);
        request.addTextFormData("description", description);
        request.addApiHeader("Authorization", "Bearer " + accessToken);

        ApiResponse response = client.executeApiRequest(request);
        log.info("Response: " + response.getResponseStr());
        
        assertTrue(response.getResponseStr().contains(title));
    } */
    
    /* @Test
    public void subirImageGettr() {
        String filePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Spa_1950.jpg";
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJsYWJyYWZhIiwidXNlcm5hbWUiOiJsYWJyYWZhIiwibGV2ZWwiOjAsImd2ZXIiOiIyRTY1OTE4RiIsImN2ZXIiOiJTUjhQMkQiLCJpYXQiOjE3MjU0NjI3ODIsImV4cCI6MjA0MDgyMjc4Mn0.7C_WwyYRFsC30xwQ5i6st988EqaTm9UmpHPC4_YNres";
        String endpoint = "https://upload.gettr.com/media/get_upload_channel";
        int okStatus1 = 200;
        int okStatus2 = 201;
        int okStatus3 = 200;
        
        ApiRequest request1 = new ApiRequest(endpoint, okStatus1, ApiMethodType.GET);
        request1.addApiHeader("Authorization", accessToken);
        request1.addApiHeader("filename", "Spa_1950.jpg");
        request1.addApiHeader("userid", "labrafa");
        
        Gson gson = new Gson();
        ApiResponse response1 = client.executeApiRequest(request1);
        GettrUploadChannel uploadChannel = gson.fromJson(response1.getResponseStr(), GettrUploadChannel.class);
        
        log.info("Tengo el canal para subir la imagen: " + uploadChannel.getGcs().getUrl());
            
        ApiRequest request2 = new ApiRequest(uploadChannel.getGcs().getUrl(), okStatus2, ApiMethodType.POST, "");
        request2.addApiHeader("Content-Type", "image/jpeg");
        request2.addApiHeader("X-Goog-Resumable", "start");
        
        ApiResponse response2 = client.executeApiRequest(request2);
        
        String imageId = null;
        List<String> headerList = response2.getHttpHeaders().get("X-GUploader-UploadID");
        if ((headerList != null) && (!headerList.isEmpty())) {
            imageId = headerList.get(0);
        }
        log.debug("He obtenido el ID de la imagen: " + imageId);
            
        File file = new File(filePath);
        ApiRequest request3 = new ApiRequest(uploadChannel.getGcs().getUrl() + "&upload_id=" + imageId, okStatus3, ApiMethodType.PUT, file);
        //request3.addApiPathParam("upload_id", imageId);
        request3.addApiHeader("Content-Type", "image/jpeg");
        String md5Str = PostUtils.calculateMD5Base64(filePath);
        request3.addApiHeader("content-md5", md5Str);

        client.executeApiRequest(request3);
        
        assertTrue(true);
    } */
}