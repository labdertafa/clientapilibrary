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
import com.laboratorio.clientapilibrary.utils.ReaderConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.conscrypt.Conscrypt;

/**
 *
 * @author Rafael
 * @version 2.2
 * @created 06/09/2024
 * @updated 03/11/2025
 */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);
    
    private static final String LINE_FEED = "\r\n";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String ERROR_LIBERANDO = "Error liberando los recursos: %s";
    
    private String cookiesFilePath;

    public ApiClient() {
        Brotli4jLoader.ensureAvailability();
    }

    public ApiClient(String cookiesFilePath) {
        Brotli4jLoader.ensureAvailability();
        this.cookiesFilePath = cookiesFilePath;
    }

    // Procesar la respuesta HTTP
    private byte[] processResponse(String contentEncoding, byte[] responseBytes) throws IOException {
        // Si la respuesta está codificada como Brotli (br)
        if ("br".equalsIgnoreCase(contentEncoding)) {
            try (InputStream inputStream = new BrotliInputStream(new ByteArrayInputStream(responseBytes))) {
                return inputStream.readAllBytes();
            }
        } else {
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                try (InputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(responseBytes))) {
                    return inputStream.readAllBytes();
                }   
            } else {
                return responseBytes;
            }
        }
    }

    private byte[] readInputStreamAsBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    private byte[] getHttpResponse(HttpURLConnection httpConn) throws IOException {
        // Se procesa la respuesta
        String contentEncoding = httpConn.getHeaderField("Content-Encoding");
        try (InputStream inputStream = httpConn.getInputStream()) {
            byte[] responseBytes = readInputStreamAsBytes(inputStream);
            return processResponse(contentEncoding, responseBytes);
        }
    }

    private void processResponseCookies(HttpURLConnection httpConn) {
        List<String> cookiesHeader = httpConn.getHeaderFields().get("Set-Cookie");

        // Almacena las cookies de la respuesta si es necesario
        if ((this.cookiesFilePath != null) && (cookiesHeader != null)) {
            CookieManager.saveCookies(this.cookiesFilePath, cookiesHeader);
        }
    }
    
    private HttpURLConnection crearConexionHTTP(String uri, ApiRequest request) throws IOException {
        HttpURLConnection httpConn;
        
        URL url = new URL(uri);
        // Se verifica que la llamada no sea para Gab
        if (uri.contains("gab.com")) {
            ReaderConfig config = new ReaderConfig("config//apiclientconfig.properties");
            String proxyDNS = config.getProperty("gab_proxy_dns");
            int proxyPort = Integer.parseInt(config.getProperty("gab_proxy_port"));
            Proxy gabProxy = new Proxy(
                    Proxy.Type.HTTP,
                    new InetSocketAddress(proxyDNS, proxyPort)
            );
            httpConn = (HttpURLConnection) url.openConnection(gabProxy);
        } else {
            httpConn = (HttpURLConnection) url.openConnection();
        }
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // habilita salida
        httpConn.setDoInput(true);  // habilita entrada
        httpConn.setRequestMethod(request.getMethod().name());
        httpConn.setConnectTimeout(10000); // 10000 milisegundos (ajustable)
        httpConn.setReadTimeout(300000);   // 300000 milisegundos (ajustable)

        httpConn.setRequestProperty("Connection", "close");
        
        return httpConn;
    }
    
    private void configurarSSLContext(String uri) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] managers = null;
        
        if (uri.contains("gab.com")) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ReaderConfig config = new ReaderConfig("config//apiclientconfig.properties");
            String certificatePath = config.getProperty("gab_proxy_certificate");
            FileInputStream fis = new FileInputStream(certificatePath);
            Certificate ca = cf.generateCertificate(fis);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("httptoolkit", ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            managers = tmf.getTrustManagers();
        }
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, managers, null);
        
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
    
    private void construirPeticion(HttpURLConnection httpConn, ApiRequest request) throws IOException {
        // Se agregan las cabeceras a la petición
        for (ApiElement element : request.getElements()) {
            if (element.getType() == ApiElementType.HEADER) {
                httpConn.setRequestProperty(element.getName(), element.getValue());
                log.debug("{}: {}", element.getName(), element.getValue());
            }
        }

        // Se agregan las cookies a la petición
        if (!request.getCookies().isEmpty()) {
            String cookiesCombined = String.join("; ", request.getCookies());
            httpConn.setRequestProperty("Cookie", cookiesCombined);
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
    }

    public ApiResponse executeApiRequest(ApiRequest request) {
        String fullUri = request.getUri() + request.getQueryParams();
        HttpURLConnection httpConn = null;
        
        try {
            // 1. Registrar Conscrypt para emular un navegador
            Security.insertProviderAt(Conscrypt.newProvider(), 1);

            // 2. Configurar SSLContext
            this.configurarSSLContext(fullUri);
        
            // 3. Iniciar la conexión
            httpConn = this.crearConexionHTTP(fullUri, request);
            
            // 4. Se construye la petición
            this.construirPeticion(httpConn, request);
            
            // 5. Se ejecuta la petición
            int responseCode = httpConn.getResponseCode();

            // 6. Se procesa la respuesta
            byte[] responseByte = this.getHttpResponse(httpConn);
            String responseStr = "";
            if (responseByte != null) {
                responseStr = new String(responseByte, StandardCharsets.UTF_8);
            }

            if (responseCode != request.getOkResponse()) {
                String str = String.format("Respuesta del error %d:. Detalle: %s", responseCode, responseStr);
                throw new ApiClientException(str);
            }

            // Se procesa la respuesta
            log.debug("Se ejecutó la solicitud: {}", fullUri);
            log.debug("Response Code de la solicitud: {}", responseCode);
            log.debug("Respuesta recibida: {}", responseStr);
            
            this.processResponseCookies(httpConn);

            return new ApiResponse(httpConn.getHeaderFields(), httpConn.getHeaderFields().get("Set-Cookie"), responseStr, responseByte);
        } catch (ApiClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiClientException("Error ejecutando la solicitud: " + fullUri, e);
        } finally {
            try {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            } catch (Exception e) {
                log.warn(String.format(ERROR_LIBERANDO, e.getMessage()));
            }
        }
    }

    private void processJsonBody(HttpURLConnection httpConn, ApiRequest request) throws IOException {
        OutputStream os = null;

        httpConn.setRequestProperty("Content-Type", "application/json");

        try {
            // Enviar el cuerpo JSON
            byte[] input = request.getPayload().getBytes(StandardCharsets.UTF_8);
            httpConn.setRequestProperty(CONTENT_LENGTH, String.valueOf(input.length));
            os = httpConn.getOutputStream();
            os.write(input, 0, input.length);
            os.flush();
        } catch (IOException e) {
            log.error("Error enviando el cuerpo de la solicitud: " + request.getUri());
            throw e;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.warn(String.format(ERROR_LIBERANDO, e.getMessage()));
            }
        }
    }

    private void processBinaryBody(HttpURLConnection httpConn, ApiRequest request) throws IOException {
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        log.debug("Connection: Keep-Alive");
        httpConn.setRequestProperty("Cache-Control", "no-cache");
        log.debug("Cache-Control: no-cache");
        httpConn.setRequestProperty(CONTENT_LENGTH, String.valueOf(request.getBinaryFile().length()));
        log.debug("Content-Length: {}", String.valueOf(request.getBinaryFile().length()));

        // Leemos el archivo binario
        try (FileInputStream fileInputStream = new FileInputStream(request.getBinaryFile())) {
            // Creamos el stream de salida para enviar los datos binarios
            try (DataOutputStream outputStream = new DataOutputStream(httpConn.getOutputStream())) {
                // Buffer para leer y enviar los bytes
                byte[] buffer = new byte[4096];
                int bytesRead;

                // Escribimos el contenido del archivo en el stream de salida
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                // Asegurarse de que se envíen todos los datos
                outputStream.flush(); 
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

        log.debug("Se agregó un elemento al Formdata: {}", builder.toString());

        return builder.toString();
    }

    public void processMultipartFormBody(HttpURLConnection httpConn, ApiRequest request) throws IOException {
        // Generar un boundary único
        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString();
        String contentType = "multipart/form-data; boundary=" + boundary;

        // Buffer temporal para calcular el Content-Length
        ByteArrayOutputStream multipart = new ByteArrayOutputStream();

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
                        try (FileInputStream inputStream = new FileInputStream(imageFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                multipart.write(buffer, 0, bytesRead);
                            }
                        }
                        multipart.writeBytes(LINE_FEED.getBytes(StandardCharsets.UTF_8));
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
            httpConn.setRequestProperty(CONTENT_LENGTH, Integer.toString(contentLength));
            log.debug("Content-Length: {}", Integer.toString(contentLength));

            try (OutputStream requestStream = httpConn.getOutputStream()) {
                multipart.writeTo(requestStream);
            }
            multipart.flush();
        } catch (IOException e) {
            log.error("Se ha producido un error procesando un formulario multi-partes");
            throw e;
        }
    }
}