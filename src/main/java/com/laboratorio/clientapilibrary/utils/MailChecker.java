package com.laboratorio.clientapilibrary.utils;

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SubjectTerm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 30/09/2024
 * @updated 30/09/2024
 */
public class MailChecker {
    protected static final Logger log = LogManager.getLogger(MailChecker.class);
    
    private MailChecker() {
    }
    
    private static void logException(Exception e) {
        log.error("Error: " + e.getMessage());
        if (e.getCause() != null) {
            log.error("Causa: " + e.getCause().getMessage());
        }
    }
    
    public static String getFirtMailByTitle(String username, String password, String title) {
        // Configuración de propiedades para conectar a Gmail
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");
        
        try {
            // Crear una sesión de correo con autenticación
            Session session = Session.getInstance(props, null);

            // Conectar al servidor de Gmail usando IMAP
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);

            // Abrir la carpeta de "INBOX"
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Se filtran los correos por el asunto definido por el parámetro "title"
            Message[] messages = inbox.search(new SubjectTerm(title));

            // Se devuelve el primer mensaje que coincida con el título buscado
            if (messages.length > 0) {
                // Si hay mensajes, mostramos los detalles del más reciente
                Message message = messages[0];
                log.info("Correo encontrado con fecha: " + message.getSentDate());
                
                String content = message.getContent().toString();
                if (message.isMimeType("text/html")) {
                    content = Jsoup.parse(content).text();
                }

                inbox.close(false);
                store.close();
                
                return content;
            }

            // Cerrar las conexiones
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            logException(e);
        }
        
        return null;
    }
}