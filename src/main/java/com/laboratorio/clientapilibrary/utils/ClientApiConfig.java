package com.laboratorio.clientapilibrary.utils;

import java.io.FileReader;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 16/08/2024
 * @updated 02/02/2025
 */
public class ClientApiConfig {
    private static final Logger log = LogManager.getLogger(ClientApiConfig.class);
    private static ClientApiConfig instance;
    private final Properties properties;

    private ClientApiConfig() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try {
            this.properties.load(new FileReader("config//apiclientconfig.properties"));
        } catch (Exception e) {
            log.error("Ha ocurrido un error leyendo el fichero de configuración del cliente API. Finaliza la aplicación!");
            log.error(String.format("Error: %s", e.getMessage()));
            if (e.getCause() != null) {
                log.error(String.format("Causa: %s", e.getCause().getMessage()));
            }
            System.exit(-1);
        }
    }

    public static ClientApiConfig getInstance() {
        if (instance == null) {
            synchronized (ClientApiConfig.class) {
                if (instance == null) {
                    instance = new ClientApiConfig();
                }
            }
        }
        
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}