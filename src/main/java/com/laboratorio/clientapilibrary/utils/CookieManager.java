package com.laboratorio.clientapilibrary.utils;

import com.laboratorio.clientapilibrary.ApiClient;
import com.laboratorio.clientapilibrary.model.ApiMethodType;
import com.laboratorio.clientapilibrary.model.ApiRequest;
import com.laboratorio.clientapilibrary.model.ApiResponse;
import com.laboratorio.clientapilibrary.model.SerializableCookie;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 2.0
 * @created 01/09/2024
 * @updated 05/10/2024
 */
public class CookieManager {
    protected static final Logger log = LogManager.getLogger(CookieManager.class);

    private static void logException(Exception e) {
        log.error("Error: " + e.getMessage());
        if (e.getCause() != null) {
            log.error("Causa: " + e.getCause().getMessage());
        }
    }
    
    public static List<SerializableCookie> parseCookies(List<String> cookiesHeader) {
        List<SerializableCookie> cookies = new ArrayList<>();

        for (String cookieHeader : cookiesHeader) {
            String[] cookieParts = cookieHeader.split(";");

            SerializableCookie cookie = new SerializableCookie();
            for (int i = 0; i < cookieParts.length; i++) {
                String part = cookieParts[i].trim();

                // El primer par nombre=valor contiene el nombre de la cookie
                if (i == 0) {
                    String[] nameValue = part.split("=", 2);
                    cookie.setName(nameValue[0]);
                    cookie.setValue(nameValue.length > 1 ? nameValue[1] : "");
                } else {
                    String[] attribute = part.split("=", 2);
                    String attributeName = attribute[0].trim().toLowerCase();
                    String attributeValue = attribute.length > 1 ? attribute[1].trim() : "";

                    switch (attributeName) {
                        case "domain" -> cookie.setDomain(attributeValue);
                        case "path" -> cookie.setPath(attributeValue);
                        case "expires" -> {
                            try {
                                DateTimeFormatter dateFormat = DateTimeFormatter.RFC_1123_DATE_TIME;
                                cookie.setExpiry(ZonedDateTime.parse(attributeValue, dateFormat));
                            } catch (Exception e) {
                                log.warn("Error al analizar la fecha de expiración de la cookie: " + e.getMessage());
                                cookie.setExpiry(ZonedDateTime.now().plusDays(1L));
                            }
                        }
                        case "version" -> cookie.setVersion(Integer.parseInt(attributeValue));
                        case "comment" -> cookie.setComment(attributeValue);
                    }
                }
            }

            cookies.add(cookie);
        }

        return cookies;
    }

    // Función para guardar las cookies en un archivo
    public static void saveCookies(String filePath, List<String> cookiesHeader) {
        try {
            List<SerializableCookie> cookies = parseCookies(cookiesHeader);
            Map<String, SerializableCookie> serializableCookies = new HashMap<>();
            for (SerializableCookie cookie : cookies) {
                serializableCookies.put(cookie.getName(), cookie);
            }

            // Se borra el fichero si existe
            File fileToDelete = new File(filePath);
            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }

            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serializableCookies);
            oos.close();
            fos.close();

            log.debug("Las cookies del website se guardaron exitosamente.");
        } catch (IOException e) {
            log.error("Error almacenando las cookies de Truth Social");
            logException(e);
        }
    }

    // Función para recuperar cookies guardadas y filtrar las que no estén expiradas
    public static List<SerializableCookie> loadCookies(String filePath) {
        List<SerializableCookie> cookies = new ArrayList<>();

        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Map<String, SerializableCookie> serializableCookies = (Map<String, SerializableCookie>) ois.readObject();
            
            ois.close();
            fis.close();
            
            for (Map.Entry<String, SerializableCookie> entry : serializableCookies.entrySet()) {
                cookies.add(entry.getValue());
            }
            
            log.debug("Las cookies de Truth Social de cargaron exitosamente.");
        } catch (Exception e) {
            log.error("Problemas al recuperar las cookies del website. Se cargará un conjunto vacío.");
            logException(e);
        }

        // Filtra cookies expiradas considerando la zona horaria local
        return cookies.stream()
                .filter(c -> c.getExpiry() == null || c.getExpiry().isAfter(ZonedDateTime.now()))
                .collect(Collectors.toList());
    }

    // Extrae la información de las cookies en una lista de cadenas
    public static List<String> extractCookiesInformation(List<SerializableCookie> cookies) {
        List<String> cookiesList = new ArrayList<>();

        for (SerializableCookie cookie : cookies) {
            cookiesList.add(cookie.toString());
        }

        return cookiesList;
    }
    
    // Obtiene las cookies del website
    public static List<String> getWebsiteCookies(String cookiesFilePath, String uri, String userAgent) {
        // Cargar las cookies almacenadas si existen
        if (cookiesFilePath != null) {
            List<SerializableCookie> existingCookies = loadCookies(cookiesFilePath);
            if (!existingCookies.isEmpty()) {
                return extractCookiesInformation(existingCookies);
            }
        }
        
        try {
            // Realiza una primera solicitud para obtener las cookies
            ApiClient client = new ApiClient();
            ApiRequest request = new ApiRequest(uri, 200, ApiMethodType.GET);
            request.addApiHeader("User-Agent", userAgent);
            ApiResponse response = client.executeApiRequest(request);

            return extractCookiesInformation(parseCookies(response.getCookies()));
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }
}