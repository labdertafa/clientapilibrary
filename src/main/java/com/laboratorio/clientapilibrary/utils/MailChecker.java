package com.laboratorio.clientapilibrary.utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.SubjectTerm;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author Rafael
 * @version 1.1
 * @created 30/09/2024
 * @updated 05/03/2025
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
    
    public static String getFirtMailByTitle(String email, String password, String title) {
        if (email.toLowerCase().contains("outlook")) {
            return getFirtMailByTitle("imap-mail.outlook.com", email, password, title);
        }
        
        return getFirtMailByTitle("imap.gmail.com", email, password, title);
    }
    
    private static String getFirtMailByTitle(String hostname, String email, String password, String title) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", hostname);
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.auth", "true");
        props.put("mail.imaps.ssl.enable", "true");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        session.setDebug(false); 
        
        try {
            // Session session = Session.getInstance(props, null);

            // Conectar al servidor de correo usando IMAP
            Store store = session.getStore("imaps");
            // store.connect(hostname, email, password);
            store.connect();

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