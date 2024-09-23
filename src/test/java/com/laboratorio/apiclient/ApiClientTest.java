package com.laboratorio.apiclient;

import com.laboratorio.clientapilibrary.ApiClient;
import com.laboratorio.clientapilibrary.exceptions.ApiClientException;
import com.laboratorio.clientapilibrary.impl.ApiClientImpl;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import java.util.List;
import javax.ws.rs.core.Response;
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
 * @updated 23/09/2024
 */
public class ApiClientTest {
    private static final Logger log = LogManager.getLogger(ApiClientTest.class);
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
    
    @Test
    public void uploadMindsImage() {
        String filePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Monza_1955.jpg";
        
        ApiRequest request = new ApiRequest("https://www.minds.com/api/v1/media", 200);
        request.addApiHeader("Accept", "application/json, text/plain, */*");
        request.addApiHeader("Accept-Encoding", "gzip, deflate, br, zstd");
        request.addApiHeader("Origin", "https://www.minds.com");
        request.addApiHeader("Referer", "https://www.minds.com/newsfeed/subscriptions/for-you");
        request.addApiHeader("Cookie", "hl=es; minds_pseudoid=muuf02mwpt7vp4wzdpowq; cw_conversation=eyJhbGciOiJIUzI1NiJ9.eyJzb3VyY2VfaWQiOiI1ZjhjZDA1YS1kYThlLTQyNDUtOTZkNS1jNWZmMTRhYzRlMDUiLCJpbmJveF9pZCI6MjY3NzB9.4WrioTFGKr37RW-mtMMcvRC08P8bCgjv2vbWi16AUnk; minds_sess=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJqdGkiOiI3ZjRjZTU3NjliNTY3M2RiNTNhNzIzNDc4MmRjYjhjNmUzZGNiN2E1MzZjOTFkMWZkMjVhMjYxZDE5NjBmMDllMzM1N2ViMjI4MGM2OWQ1MDBlMDAyMjU1OTViMDQyZTYzOTJjMDU5MGZkOGJhY2MyN2JjYTcyODIyNjMzODM0YSIsImV4cCI6MTc1ODE5MjY3Ny4yMTIxNTgsInN1YiI6IjE2NzYxOTQ3OTYwNjgxNDcyMDciLCJ1c2VyX2d1aWQiOiIxNjc2MTk0Nzk2MDY4MTQ3MjA3In0.e0D7MWSbfLV0-4SGY1zBLTbIhCWxIzlyZuWBvqr8HbEiyOGrDbZdTYCcMIG2O4XQMpWjnDZ4OYtJtEpLHBY9iMBzNwKywBhNqCcZu2kxKUd65LK89bi7nJBDUjxTlLeUbn55mcBMcCxmqknDpYZjvZZK1OPKuJ5b2L6CUfZWhFOz_wrtDQvlguLMJG_kvFf9n_B3yW21aYZcO9de-j2ADhrZWKgJg6iukDCAaduVhiHp2wTXXrA87oFBJOtATH0-jIajgRwzuGv0JMJGPhjqCEH54KljkoEzNjLiFofRG-jeyPMtvwpjwJBRoPcueGQSQ9wj9O9GPabAKQ4z-tvHEg; cw_user_hTsptQ5idnWS11PVaq1EbT1n=e04519183ad004e2668b6bd5ba86fc9d; XSRF-TOKEN=bd2fd9ea2d3bad7376499ebe85730297e4600347bbf049a6f3def8087d48a71a612ea93e139dd219e5873a8a16a9ee87e82ec441064490898b24551d9444592d; __cf_bm=2yX9RSHGSuuw59iOo0eGuMQ3o0vHYu2xuZ.coFlS.go-1726989635-1.0.1.1-Q4QIbMTE3A9a3rKmoeGH7NEVLvryb56kpjGeLAsLZaYd0uPObd08Ujm1P4h3MLSSlwnJv23iwpwpm0MdRVZEbQ; ph_phc_GBvowRy7KExnom0MfTPgibtCdP4ZRlRb0szn1fLSCeU_posthog=%7B%22distinct_id%22%3A%221676194796068147207%22%2C%22%24sesid%22%3A%5B1726989643125%2C%220192187f-7e92-7025-be6e-8a9a78da025b%22%2C1726987861650%5D%7D");
        request.addApiHeader("X-Version", "1452425022");
        request.addApiHeader("X-Xsrf-Token", "bd2fd9ea2d3bad7376499ebe85730297e4600347bbf049a6f3def8087d48a71a612ea93e139dd219e5873a8a16a9ee87e82ec441064490898b24551d9444592d");
        request.addFileFormData("file", filePath);
        
        String jsonStr = client.executePostMultipartForm(request);
        log.info("Respuesta: " + jsonStr);
        
        assertTrue(jsonStr.contains("success"));
    }
}