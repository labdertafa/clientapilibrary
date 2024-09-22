package com.laboratorio.clientapilibrary.impl;

import java.io.IOException;
import java.util.Map;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 22/09/2024
 * @updated 22/09/2024
 */
public class ContentTypeCheckerFilter implements ClientRequestFilter {
    protected static final Logger log = LogManager.getLogger(ContentTypeCheckerFilter.class);
    
    @Override
    public void filter(ClientRequestContext crc) throws IOException {
        log.debug("HTTP Request Headers:");

        for (Map.Entry<String, java.util.List<Object>> entry : crc.getHeaders().entrySet()) {
            log.debug(entry.getKey() + ": " + entry.getValue());
        }
    }
}