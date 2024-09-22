package com.laboratorio.clientapilibrary.impl;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.laboratorio.clientapilibrary.ApiClient;
import com.laboratorio.clientapilibrary.exceptions.ApiClientException;
import com.laboratorio.clientapilibrary.model.ApiElement;
import com.laboratorio.clientapilibrary.model.ApiElementType;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import com.laboratorio.clientapilibrary.model.ApiValueType;
import com.laboratorio.clientapilibrary.model.ProcessedResponse;
import com.laboratorio.clientapilibrary.utils.CookieManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataWriter;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 06/09/2024
 * @updated 22/09/2024
 */

public class ApiClientImpl implements ApiClient {
    private static final Logger log = LogManager.getLogger(ApiClientImpl.class);
    private String cookiesFilePath;

    public ApiClientImpl() {
        Brotli4jLoader.ensureAvailability();
    }

    public ApiClientImpl(String cookiesFilePath) {
        Brotli4jLoader.ensureAvailability();
        this.cookiesFilePath = cookiesFilePath;
    }
    
    protected void logException(Exception e) {
        log.error("Error: " + e.getMessage());
        if (e.getCause() != null) {
            log.error("Causa: " + e.getCause().getMessage());
            if (e.getCause().getCause() != null) {
                log.error("Causa: " + e.getCause().getCause().getMessage());
            }
        }
    }
    
    // Obtiene las cookies del website
    @Override
    public List<String> getWebsiteCookies(String uri) {
        // Cargar las cookies almacenadas si existen
        if (this.cookiesFilePath != null) {
            Map<String, NewCookie> existingCookies = CookieManager.loadCookies(this.cookiesFilePath);
            if (!existingCookies.isEmpty()) {
                return CookieManager.extractCookiesInformation(existingCookies);
            }
        }
        
        ResteasyClient client = new ResteasyClientBuilderImpl()
                .enableCookieManagement()
                .build();
        Response response = null;
        
        try {
            // Realiza una primera solicitud para obtener las cookies
            ResteasyWebTarget target = client.target(uri);
            response = target.request().get();

            return CookieManager.extractCookiesInformation(response.getCookies());
        } catch (Exception e) {
            logException(e);
            throw e;
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }
    
    // Procesar la respuesta HTTP
    private String processResponse(Response response) {
        try {
            // Obtén el InputStream de la entidad y descomprímelo si es necesario
            String contentEncoding = response.getHeaderString("Content-Encoding");
            
            // Leer el cuerpo de la respuesta como bytes
            byte[] responseBytes = response.readEntity(byte[].class);
            
            // Si la respuesta está codificada como Brotli (br)
            if ("br".equalsIgnoreCase(contentEncoding)) {
                InputStream brotliInputStream = new BrotliInputStream(new ByteArrayInputStream(responseBytes));
                byte[] decompressedBytes = brotliInputStream.readAllBytes();
                return new String(decompressedBytes, StandardCharsets.UTF_8);
            } else {
                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    InputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(responseBytes));
                    byte[] decompressedBytes = gzipInputStream.readAllBytes();
                    return new String(decompressedBytes, StandardCharsets.UTF_8);
                } else {
                    return new String(responseBytes, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            throw new ApiClientException(ApiClientImpl.class.getName(), "Error procesando la respuesta recibida");
        } finally {
            // Almacena las cookies de la respuesta
            if (this.cookiesFilePath != null) {
                CookieManager.saveCookies(this.cookiesFilePath, response.getCookies());
            }
        }
    }
    
    private WebTarget createTarget(Client client, ApiRequest request) {
        WebTarget target = client.target(request.getUri());
        
        if (request.isFormData()) {
            target = target.register(MultipartFormDataWriter.class);
        }
        
        for (ApiElement element : request.getElements()) {
            if (element.getType() == ApiElementType.PATHPARAM) {
                target = target.queryParam(element.getName(), element.getValue());
            }
        }
        
        log.debug("Se ha creado el target de la solicitud correctamente");
        
        return target;
    }
    
    private Invocation.Builder createInvocation(WebTarget target, ApiRequest request) {
        Invocation.Builder requestBuilder = target.request();
        for (ApiElement element : request.getElements()) {
            if (element.getType() == ApiElementType.HEADER) {
                requestBuilder.header(element.getName(), element.getValue());
            }
        }

        for (String cookie : request.getCookies()) {
            requestBuilder.cookie(Cookie.valueOf(cookie));
        }
        
        log.debug("Se ha creado el Invocation de la solicitud correctamente");
        
        return requestBuilder;
    }
    
    private ProcessedResponse executeSimpleGetRequest(ApiRequest request) throws ApiClientException {
        Client client = ClientBuilder.newClient();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            response = requestBuilder.get();
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }
    
    @Override
    public String executeGetRequest(ApiRequest request) throws ApiClientException {
        return this.executeSimpleGetRequest(request).getResponseDetail();
    }
    
    @Override
    public Response getResponseGetRequest(ApiRequest request) throws ApiClientException {
        return this.executeSimpleGetRequest(request).getResponse();
    }
    
    @Override
    public ProcessedResponse getProcessedResponseGetRequest(ApiRequest request) throws ApiClientException {
        return this.executeSimpleGetRequest(request);
    }
    
    // Ejecuta un POST que tiene un BODY con un JSON
    private ProcessedResponse executePostRequestWithJSONBody(ApiRequest request) throws ApiClientException {
        Client client = ClientBuilder.newClient();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            if (request.getPayload() == null) {
                response = requestBuilder.post(Entity.text(""));
            } else {
                response = requestBuilder
                        .post(Entity.entity(request.getPayload(), MediaType.APPLICATION_JSON));
            }
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }
    
    // Ejecuta un POST que tiene un BODY con un fichero binario
    private ProcessedResponse executePostRequestWithBinaryBody(ApiRequest request) throws ApiClientException {
        Client client = ClientBuilder.newClient();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            File file = new File(request.getBinaryFile());
            InputStream fileStream = new FileInputStream(file);
            
            response = requestBuilder
                    .post(Entity.entity(fileStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }
    
    // Ejecuta un POST con una FORMDATA
    private ProcessedResponse executePostRequesFormData(ApiRequest request) throws ApiClientException {
        ResteasyClient client = (ResteasyClient)ResteasyClientBuilder.newBuilder().build();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            MultipartFormDataOutput formDataOutput = new MultipartFormDataOutput();
            for (ApiElement element : request.getElements()) {
                if (element.getType() == ApiElementType.FORMDATA) {
                    if (element.getValueType() == ApiValueType.FILE) {
                        File imageFile = new File(element.getValue());
                        InputStream fileStream = new FileInputStream(imageFile);
                        formDataOutput.addFormData(element.getName(), fileStream, MediaType.APPLICATION_OCTET_STREAM_TYPE, imageFile.getName());
                    } else {
                        if (element.getValueType() == ApiValueType.JSON) {
                            formDataOutput.addFormData(element.getName(), element.getValue(), MediaType.APPLICATION_JSON_TYPE);
                        } else {
                            formDataOutput.addFormData(element.getName(), element.getValue(), MediaType.TEXT_PLAIN_TYPE);
                        }
                    }
                }
            }

            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            response = requestBuilder
                    .post(Entity.entity(formDataOutput, MediaType.MULTIPART_FORM_DATA));
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }

    @Override
    public String executePostRequest(ApiRequest request) throws ApiClientException {
        if (request.isFormData()) {
            return this.executePostRequesFormData(request).getResponseDetail();
        } else {
            if (request.getBinaryFile() != null) {
                return this.executePostRequestWithBinaryBody(request).getResponseDetail();
            }
        }
        
        return this.executePostRequestWithJSONBody(request).getResponseDetail();
    }

    @Override
    public Response getResponsePostRequest(ApiRequest request) throws ApiClientException {
        if (request.isFormData()) {
            return this.executePostRequesFormData(request).getResponse();
        } else {
            if (request.getBinaryFile() != null)  {
                return this.executePostRequestWithBinaryBody(request).getResponse();
            }
        }
        
        return this.executePostRequestWithJSONBody(request).getResponse();
    }
    
    @Override
    public ProcessedResponse getProcessedResponsePostRequest(ApiRequest request) throws ApiClientException {
        if (request.isFormData()) {
            return this.executePostRequesFormData(request);
        } else {
            if (request.getBinaryFile() != null)  {
                return this.executePostRequestWithBinaryBody(request);
            }
        }
        
        return this.executePostRequestWithJSONBody(request);
    }
    
    // Ejecuta un PUT que tiene un BODY con un JSON
    private ProcessedResponse executePutRequestWithJSONBody(ApiRequest request) throws ApiClientException {
        Client client = ClientBuilder.newClient();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            if (request.getPayload() == null) {
                response = requestBuilder.put(Entity.text(""));
            } else {
                response = requestBuilder
                        .put(Entity.entity(request.getPayload(), MediaType.APPLICATION_JSON));
            }
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }
    
    // Ejecuta un PUT que tiene un BODY con un fichero binario
    private ProcessedResponse executePutRequestWithBinaryBody(ApiRequest request) throws ApiClientException {
        Client client = ClientBuilder.newClient();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            File file = new File(request.getBinaryFile());
            InputStream fileStream = new FileInputStream(file);
            
            response = requestBuilder
                    .put(Entity.entity(fileStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }
    
    @Override
    public String executePutRequest(ApiRequest request) throws ApiClientException {
        if (request.isFormData()) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        } else {
            if (request.getBinaryFile() != null) {
                return this.executePutRequestWithBinaryBody(request).getResponseDetail();
            }
        }
        
        return this.executePutRequestWithJSONBody(request).getResponseDetail();
    }

    @Override
    public Response getResponsePutRequest(ApiRequest request) throws ApiClientException {
        if (request.isFormData()) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        } else {
            if (request.getBinaryFile() != null) {
                return this.executePutRequestWithBinaryBody(request).getResponse();
            }
        }
        
        return this.executePutRequestWithJSONBody(request).getResponse();
    }
    
    @Override
    public ProcessedResponse getProcessedResponsePutRequest(ApiRequest request) throws ApiClientException {
        if (request.isFormData()) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        } else {
            if (request.getBinaryFile() != null)  {
                return this.executePutRequestWithBinaryBody(request);
            }
        }
        
        return this.executePutRequestWithJSONBody(request);
    }
    
    private ProcessedResponse executeSimpleDeleteRequest(ApiRequest request) throws ApiClientException {
        Client client = ClientBuilder.newClient();
        client.register(ContentTypeCheckerFilter.class);
        Response response = null;
        
        try {
            WebTarget target = createTarget(client, request);
            
            Invocation.Builder requestBuilder = this.createInvocation(target, request);
            
            response = requestBuilder.delete();
            
            String responseStr = this.processResponse(response);
            if (response.getStatus() != request.getOkResponse()) {
                log.error(String.format("Respuesta del error %d: %s", response.getStatus(), responseStr));
                String str = "Error ejecutando: " + request.getUri() + ". Se obtuvo el código de error: " + response.getStatus();
                throw new Exception(str);
            }
            
            log.debug("Se ejecutó la query: " + request.getUri());
            log.debug("Respuesta JSON recibida: " + responseStr);
            
            return new ProcessedResponse(response, responseStr);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            logException(e);
            throw new ApiClientException(ApiClientImpl.class.getName(), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            client.close();
        }
    }

    @Override
    public String executeDeleteRequest(ApiRequest request) {
        return this.executeSimpleDeleteRequest(request).getResponseDetail();
    }

    @Override
    public Response getResponseDeleteRequest(ApiRequest request) throws ApiClientException {
        return this.executeSimpleDeleteRequest(request).getResponse();
    }

    @Override
    public ProcessedResponse getProcessedResponseDeleteRequest(ApiRequest request) throws ApiClientException {
        return this.executeSimpleDeleteRequest(request);
    }
}