package com.laboratorio.clientapilibrary.utils;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 16/08/2024
 * @updated 04/05/2025
 */
public class ReaderConfig {
    private static final Logger log = LogManager.getLogger(ReaderConfig.class);
    private final Properties properties;

    public ReaderConfig(String filePath) {
        this.properties = new Properties();
        this.loadProperties(filePath);
    }
    
    private void loadProperties(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            this.properties.load(fis);
        } catch (Exception e) {
            log.error("Ha ocurrido un error leyendo el fichero de configuración: {}. Finaliza la aplicación!", filePath);
            log.error("Error: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Causa: {}", e.getCause().getMessage());
            }
            System.exit(-1);
        }
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }
}