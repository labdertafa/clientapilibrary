package com.laboratorio.clientapilibrary.utils;

import com.laboratorio.clientapilibrary.model.SerializableCookie;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.NewCookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 01/09/2024
 * @updated 06/09/2024
 */
public class CookieManager {
    protected static final Logger log = LogManager.getLogger(CookieManager.class);

    private static void logException(Exception e) {
        log.error("Error: " + e.getMessage());
        if (e.getCause() != null) {
            log.error("Causa: " + e.getCause().getMessage());
        }
    }

    // Convierte una NewCookie a SerializableCookie
    private static SerializableCookie toSerializableCookie(NewCookie newCookie) {
        ZonedDateTime expiryUTC = newCookie.getExpiry() == null ? null
                : ZonedDateTime.ofInstant(newCookie.getExpiry().toInstant(), ZoneId.of("UTC"));

        return new SerializableCookie(
                newCookie.getName(),
                newCookie.getValue(),
                newCookie.getDomain(),
                newCookie.getVersion(),
                newCookie.getPath(),
                newCookie.getComment(),
                expiryUTC);
    }

    // Convierte un SerializableCookie a NewCookie
    private static NewCookie toNewCookie(SerializableCookie serializableCookie) {
        int segundosRestantes = 0;
        
        if (serializableCookie.getExpiry() != null) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            Instant nowInstant = now.toInstant();
            Instant metaInstant = serializableCookie.getExpiry().toInstant();
            Duration duration = Duration.between(nowInstant, metaInstant);
            segundosRestantes = (int) duration.getSeconds();
        }

        return new NewCookie(
                serializableCookie.getName(),
                serializableCookie.getValue(),
                serializableCookie.getPath(),
                serializableCookie.getDomain(),
                serializableCookie.getVersion(),
                serializableCookie.getComment(),
                segundosRestantes,
                false);
    }

    // Función para guardar las cookies en un archivo
    public static void saveCookies(String filePath, Map<String, NewCookie> cookies) {
        try {
            Map<String, SerializableCookie> serializableCookies = new HashMap<>();
            for (Map.Entry<String, NewCookie> entry : cookies.entrySet()) {
                serializableCookies.put(entry.getKey(), toSerializableCookie(entry.getValue()));
            }

            // Se borra el fichero si existe
            File fileToDelete = new File(filePath);
            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }

            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serializableCookies);

            log.info("Las cookies de Truth Social se guardaron exitosamente.");
        } catch (IOException e) {
            log.error("Error almacenando las cookies de Truth Social");
            logException(e);
        }
    }

    // Función para recuperar cookies guardadas y filtrar las que no estén expiradas
    public static Map<String, NewCookie> loadCookies(String filePath) {
        Map<String, NewCookie> cookies = new HashMap<>();

        try {
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Map<String, SerializableCookie> serializableCookies = (Map<String, SerializableCookie>) ois.readObject();
            for (Map.Entry<String, SerializableCookie> entry : serializableCookies.entrySet()) {
                cookies.put(entry.getKey(), toNewCookie(entry.getValue()));
            }
            
            // Filtra cookies expiradas considerando la zona horaria local
            cookies.entrySet().removeIf(entry -> {
                NewCookie cookie = entry.getValue();
                Date expiry = cookie.getExpiry();

                if (expiry != null) {
                    // Convierte la fecha de expiración a la zona horaria local
                    ZonedDateTime expiryInLocalZone = ZonedDateTime.ofInstant(expiry.toInstant(), ZoneId.systemDefault());

                    // Compara con la fecha y hora actual
                    return expiryInLocalZone.toInstant().isBefore(ZonedDateTime.now().toInstant());
                }

                return false;
            });

            log.info("Las cookies de Truth Social de cargaron exitosamente.");
        } catch (Exception e) {
            log.error("Problemas al recuperar las cookies del website. Se cargará un conjunto vacío.");
            logException(e);
        }

        return cookies;
    }

    // Extrae la información de las cookies en una lista de cadenas
    public static List<String> extractCookiesInformation(Map<String, NewCookie> cookies) {
        List<String> cookiesList = new ArrayList<>();

        for (Map.Entry<String, NewCookie> cookieEntry : cookies.entrySet()) {
            String cookieName = cookieEntry.getKey();
            NewCookie cookie = cookieEntry.getValue();
            cookiesList.add(cookieName + "=" + cookie.getValue());
        }

        return cookiesList;
    }
}