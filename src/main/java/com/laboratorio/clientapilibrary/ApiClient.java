package com.laboratorio.clientapilibrary;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.laboratorio.clientapilibrary.exceptions.ApiClientException;
import com.laboratorio.clientapilibrary.model.ApiElement;
import com.laboratorio.clientapilibrary.model.ApiElementType;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import com.laboratorio.clientapilibrary.model.ApiResponse;
import com.laboratorio.clientapilibrary.model.ApiValueType;
import com.laboratorio.clientapilibrary.utils.CookieManager;
import com.laboratorio.clientapilibrary.utils.ImageMetadata;
import com.laboratorio.clientapilibrary.utils.PostUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 2.0
 * @created 06/09/2024
 * @updated 05/10/2024
 */
public class ApiClient {
    private static final Logger log = LogManager.getLogger(ApiClient.class);
    private final String LINE_FEED = "\r\n";
    private String cookiesFilePath;

    public ApiClient() {
        Brotli4jLoader.ensureAvailability();
    }

    public ApiClient(String cookiesFilePath) {
        Brotli4jLoader.ensureAvailability();
        this.cookiesFilePath = cookiesFilePath;
    }
    
    private void logException(Exception e) {
        log.error("Error: " + e.getMessage());
        if (e.getCause() != null) {
            log.error("Causa: " + e.getCause().getMessage());
            if (e.getCause().getCause() != null) {
                log.error("Causa: " + e.getCause().getCause().getMessage());
            }
        }
    }
    
    // Procesar la respuesta HTTP
    private String processResponse(String contentEncoding, byte[] responseBytes) throws IOException {
        InputStream inputStream = null;
        
        try {
            // Si la respuesta está codificada como Brotli (br)
            if ("br".equalsIgnoreCase(contentEncoding)) {
                inputStream = new BrotliInputStream(new ByteArrayInputStream(responseBytes));
                byte[] decompressedBytes = inputStream.readAllBytes();
                return new String(decompressedBytes, StandardCharsets.UTF_8);
            } else {
                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    inputStream = new GZIPInputStream(new ByteArrayInputStream(responseBytes));
                    byte[] decompressedBytes = inputStream.readAllBytes();
                    return new String(decompressedBytes, StandardCharsets.UTF_8);
                } else {
                    return new String(responseBytes, StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            log.error("Error procesando la respuesta recibida para la solicitud");
            throw e;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.warn("Error liberando los recursos: " + e.getMessage());
            }
        }
    }
    
    private byte[] readInputStreamAsBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw e;
        }  finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (Exception e) {
                log.warn("Error liberando los recursos: " + e.getMessage());
            }
        }
    }
    
    private String getHttpResponse(HttpURLConnection httpConn) throws IOException {
        InputStream inputStream = null;
        
        // Se procesa la respuesta
        try {
            String contentEncoding = httpConn.getHeaderField("Content-Encoding");
            inputStream = httpConn.getInputStream();
            if (inputStream == null) {
                return "";
            }
            byte[] responseBytes = readInputStreamAsBytes(inputStream);

            return processResponse(contentEncoding, responseBytes);
        } catch (IOException e) {
            throw  e;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                log.warn("Error liberando los recursos: " + e.getMessage());
            }
        }
    }
    
    private List<String> processResponseCookies(HttpURLConnection httpConn) {
        Map<String, List<String>> headerFields = httpConn.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        
        // Almacena las cookies de la respuesta si es necesario
        if ((this.cookiesFilePath != null) && (cookiesHeader != null)) {
            CookieManager.saveCookies(this.cookiesFilePath, cookiesHeader);
        }
        
        return cookiesHeader;
    }
    
    public ApiResponse executeApiRequest(ApiRequest request) {
        HttpURLConnection httpConn = null;
        String uri = request.getUri() + request.getQueryParams();
        
        try {
            URL url = new URL(request.getUri() + request.getQueryParams());
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // habilita salida
            httpConn.setDoInput(true);  // habilita entrada
            httpConn.setRequestMethod(request.getMethod().name());
            
            // Se agregan las cabeceras a la petición
            for (ApiElement element : request.getElements()) {
                if (element.getType() == ApiElementType.HEADER) {
                    httpConn.setRequestProperty(element.getName(), element.getValue());
                    log.debug(element.getName() + ": " + element.getValue());
                }
            }
            
            // Se agregan las cookies a la petición
            for (String cookie : request.getCookies()) {
                httpConn.setRequestProperty("Cookie", cookie);
            }
            
            // Se contruye el body de la petición
            if (request.getPayload() != null) {         // El cuerpo es un JSON
                this.processJsonBody(httpConn, request);
            } else {
                if (request.getBinaryFile() != null) {  // El cuerpo es un fichero binario
                    this.processBinaryBody(httpConn, request);
                } else {
                    if (request.isFormData()) {         // El cuerpo es un FormData
                        this.processMultipartFormBody(httpConn, request);
                    }
                }
            }
            
            // Se ejecuta la petición
            int responseCode = httpConn.getResponseCode();
            
            // Se procesa la respuesta
            String responseStr = this.getHttpResponse(httpConn);
            
            if (responseCode != request.getOkResponse()) {
                String str = String.format("Respuesta del error %d:. Detalle: ", responseCode, responseStr);
                throw new ApiClientException(ApiClient.class.getName(), str);
            }
            
            // Se procesa la respuesta
            log.debug("Se ejecutó la solicitud: " + uri);
            log.debug("Response Code de la solicitud: " + responseCode);
            log.debug("Respuesta recibida: " + responseStr);
            
            return new ApiResponse(httpConn.getHeaderFields(), this.processResponseCookies(httpConn), responseStr);
        } catch (Exception e) {
            log.error("Error ejecutando la solicitud: " + uri);
            logException(e);
            throw new ApiClientException(ApiClient.class.getName(), e.getMessage());
        } finally {
            try {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            } catch (Exception e) {
                log.warn("Error liberando los recursos: " + e.getMessage());
            }
        }
    }
    
    private void processJsonBody(HttpURLConnection httpConn, ApiRequest request) throws IOException {
        OutputStream os = null;
        
        httpConn.setRequestProperty("Content-Type", "application/json");
        
        try  {
            // Enviar el cuerpo JSON
            byte[] input = request.getPayload().getBytes(StandardCharsets.UTF_8);
            httpConn.setRequestProperty("Content-Length", String.valueOf(input.length));
            os = httpConn.getOutputStream();
            os.write(input, 0, input.length);
        } catch (IOException e) {
            log.error("Error enviando el cuerpo de la solicitud: " + request.getUri());
            throw e;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                log.warn("Error liberando los recursos: " + e.getMessage());
            }
        }
    }
    
    private void processBinaryBody(HttpURLConnection httpConn, ApiRequest request) throws IOException {
        FileInputStream fileInputStream = null;
        DataOutputStream outputStream = null;

        try {
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
           httpConn.setRequestProperty("Content-Length", String.valueOf(request.getBinaryFile().length()));

            // Leemos el archivo binario
            fileInputStream = new FileInputStream(request.getBinaryFile());

            // Creamos el stream de salida para enviar los datos binarios
            outputStream = new DataOutputStream(httpConn.getOutputStream());

            // Buffer para leer y enviar los bytes
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Escribimos el contenido del archivo en el stream de salida
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush(); // Asegurarse de que se envíen todos los datos
        } catch (IOException e) {
            log.error("Error cargando el fichero de la solicitud: " + request.getUri());
            throw e;
        } finally {
            // Cerrar todos los recursos
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.warn("Error liberando los recursos: " + e.getMessage());
            }
        }
    }
    
    private String getFormdataElementHeader(String boundary, ApiElement element) {
        StringBuilder builder = new StringBuilder();
        
        if (element.getValueType() == ApiValueType.FILE) {
            ImageMetadata metadata = PostUtils.extractImageMetadata(element.getValue());
            File imageFile = new File(element.getValue());
            builder.append("--").append(boundary).append(LINE_FEED);
            builder.append("Content-Disposition: form-data; name=\"");
            builder.append(element.getName()).append("\"; filename=\"").append(imageFile.getName()).append("\"").append(LINE_FEED);
            builder.append("Content-Type: ").append(metadata.getMimeType()).append(LINE_FEED);
            builder.append(LINE_FEED);
        } else {
            builder.append("--").append(boundary).append(LINE_FEED);
            builder.append("Content-Disposition: form-data; name=\"").append(element.getName()).append("\"").append(LINE_FEED);
            builder.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
            builder.append(LINE_FEED);
        }
        
        log.debug("Se agregó un elemento al Formdata: " + builder.toString());
        
        return builder.toString();
    }
    
    public void processMultipartFormBody(HttpURLConnection httpConn, ApiRequest request) {
        // Generar un boundary único
        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString();
        String contentType = "multipart/form-data; boundary=" + boundary;
        
        // Buffer temporal para calcular el Content-Length
        ByteArrayOutputStream multipart = new ByteArrayOutputStream();
        // DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        
        try {
            httpConn.setRequestProperty("Content-Type", contentType);
            log.debug("Content-Type: " + contentType);
            
            // Se envía los elementos del formulario
            for (ApiElement element : request.getElements()) {
                if (element.getType() == ApiElementType.FORMDATA) {
                    String elementHeader = this.getFormdataElementHeader(boundary, element);
                    multipart.writeBytes(elementHeader.getBytes(StandardCharsets.UTF_8));
                    
                    // Se agrega el valor del elemento
                    if (element.getValueType() == ApiValueType.FILE) {
                        File imageFile = new File(element.getValue());
                        FileInputStream inputStream = new FileInputStream(imageFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            multipart.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                    } else {
                        String temp = element.getValue() + LINE_FEED;
                        multipart.writeBytes(temp.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

            // Terminar la solicitud multipart
            String temp = "--" + boundary + "--" + LINE_FEED;
            multipart.writeBytes(temp.getBytes(StandardCharsets.UTF_8));
            
            // Calculamos el tamaño total del contenido
            int contentLength = multipart.size();
            httpConn.setRequestProperty("Content-Length", Integer.toString(contentLength));
            log.debug("Content-Length: " + Integer.toString(contentLength));
            
            // log.info(LINE_FEED + byteArrayOutputStream.toString());
            try (OutputStream requestStream = httpConn.getOutputStream()) {
                multipart.writeTo(requestStream);
            }
        } catch (Exception e) {
            log.error("Se ha producido un error procesando un formulario multi-partes");
            logException(e);
            throw new ApiClientException(ApiClient.class.getName(), e.getMessage());
        }
    }
}