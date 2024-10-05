package com.laboratorio.clientapilibrary.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.laboratorio.clientapilibrary.exceptions.UtilsApiException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 07/09/2024
 * @updated 04/10/2024
 */
public class PostUtils {
    private static final Logger log = LogManager.getLogger(PostUtils.class);

    private PostUtils() {
    }

    private static void logException(Exception e) {
        log.error("Error: " + e.getMessage());
        if (e.getCause() != null) {
            log.error("Causa: " + e.getCause().getMessage());
            if (e.getCause().getCause() != null) {
                log.error("Causa: " + e.getCause().getCause().getMessage());
            }
        }
    }

    public static List<ElementoPost> extraerElementosPost(String texto) {
        List<ElementoPost> resultados = new ArrayList<>();
        if (texto == null) {
            return resultados;
        }

        // Expresión regular para URLs y hashtags
        Pattern pattern = Pattern.compile("(https?://\\S+)|(#\\w+)");
        Matcher matcher = pattern.matcher(texto);

        // Buscar elmentos en el texto
        int lastEnd = 0;
        int start = 0;
        int end = 0;
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            if (lastEnd < start) {
                resultados.add(new ElementoPost(start, end, TipoElementoPost.Normal, texto.substring(lastEnd, start)));
            }

            String found = matcher.group();

            // Se busca la posición en bytes del elementos
            int byteStart = texto.substring(0, matcher.start()).getBytes(StandardCharsets.UTF_8).length;
            int byteEnd = texto.substring(0, matcher.end()).getBytes(StandardCharsets.UTF_8).length;

            TipoElementoPost type = TipoElementoPost.Link;
            if (found.startsWith("#")) {
                type = TipoElementoPost.Tag;
            }

            resultados.add(new ElementoPost(byteStart, byteEnd, type, found));
            lastEnd = end;
        }

        // Si queda texto después de la última coincidencia, lo agregamos
        if (lastEnd < texto.length()) {
            resultados.add(new ElementoPost(start, end, TipoElementoPost.Normal, texto.substring(lastEnd)));
        }

        return resultados;
    }

    public static Map<String, String> getUrlMetadata(String url) {
        Map<String, String> metadata = new HashMap<>();

        try {
            // Conecta a la URL y obtiene el documento HTML
            Document doc = Jsoup.connect(url).get();

            // Extrae el título
            String title = doc.title();
            metadata.put("title", title);

            // Extrae la descripción
            Element description = doc.selectFirst("meta[name=description]");
            if (description != null) {
                metadata.put("description", description.attr("content"));
            } else {
                Element ogTitle = doc.selectFirst("meta[property=og:title]");
                if (ogTitle != null) {
                    metadata.put("description", ogTitle.attr("content"));
                } else {
                    Element h1Tag = doc.selectFirst("h1");
                    if (h1Tag != null) {
                        metadata.put("description", h1Tag.text());
                    } else {
                        Element h2Tag = doc.selectFirst("h2");
                        if (h2Tag != null) {
                            metadata.put("description", h2Tag.text());
                        }
                    }
                }
            }

            Element ogImage = doc.selectFirst("meta[property=og:image]");
            if (ogImage != null) {
                metadata.put("previmg", ogImage.attr("content"));
            }
            
            log.debug("Se extrajo la metadata de la url: " + url);
        } catch (IOException e) {
            log.error("Error extrayendo la metadata de la URL " + url);
            logException(e);
        }
        return metadata;
    }

    /* Almacena una imagen de una URL en un directorio pasado por parámetro */
    public static void downloadImage(String imageUrl, String destinationFile) throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            // Crear la URL a partir del enlace de la imagen
            URL url = new URL(imageUrl);

            // Abrir la conexión HTTP
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Verificar el código de respuesta
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Obtener el input stream de la conexión
                inputStream = new BufferedInputStream(connection.getInputStream());
                fileOutputStream = new FileOutputStream(destinationFile);

                // Buffer para almacenar los datos leídos
                byte[] buffer = new byte[4096];
                int bytesRead;

                // Leer desde el input stream y escribir en el archivo
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                log.debug("Imagen descargada correctamente a: " + destinationFile);
            } else {
                log.error("Error en la descarga. Código de respuesta: " + responseCode);
            }
        } catch (IOException e) {
            log.error("Error descargando imagen: " + e.getMessage());
            logException(e);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /* Extrae la imagen de la metadata del primer enlace encontrado en los elementos del Post */
    public static String getThumbnail(List<ElementoPost> elementos, String destination) {
        try {
            for (ElementoPost elem : elementos) {
                if (elem.getType() == TipoElementoPost.Link) {
                    Map<String, String> metadata = getUrlMetadata(elem.getContenido());
                    String imageUrl = metadata.get("previmg");
                    if (imageUrl != null) {
                        downloadImage(imageUrl, destination);
                        return destination;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public static String calculateMD5Base64(String filePath) {
        try {
            // Crear un MessageDigest para MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Leer el archivo y actualizar el MessageDigest
            try (InputStream is = new FileInputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }

            // Obtener el hash en forma de byte array
            byte[] md5Bytes = md.digest();

            // Convertir el byte array a Base64
            return Base64.getEncoder().encodeToString(md5Bytes);

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new UtilsApiException(PostUtils.class.getName(), "Error al calcular el checksum MD5 del archivo " + filePath, e);
        }
    }

    private static int extraerNumeros(String texto) {
        // Expresión regular para extraer números
        Pattern numberPattern = Pattern.compile("\\d+");

        Matcher matcher = numberPattern.matcher(texto);
        matcher.find();
        String numbers = matcher.group();

        return Integer.parseInt(numbers);
    }

    public static ImageMetadata extractImageMetadata(String filePath) {
        try {
            // Ruta al archivo JPG
            File file = new File(filePath);

            // Leer la metadata de la imagen
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            ImageMetadata imageMetadata = new ImageMetadata();

            // Recorrer todos los directorios de metadata
            for (Directory directory : metadata.getDirectories()) {
                // Recorrer todas las etiquetas en cada directorio
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagName().equalsIgnoreCase("Image Height")) {
                        imageMetadata.setHeight(extraerNumeros(tag.getDescription()));
                    }
                    if (tag.getTagName().equalsIgnoreCase("Image Width")) {
                        imageMetadata.setWidth(extraerNumeros(tag.getDescription()));
                    }
                    if (tag.getTagName().equalsIgnoreCase("Detected MIME Type")) {
                        imageMetadata.setMimeType(tag.getDescription());
                    }
                    if (tag.getTagName().equalsIgnoreCase("File Size")) {
                        imageMetadata.setSize(extraerNumeros(tag.getDescription()));
                    }
                    // Imprimir nombre de la etiqueta y su valor
                    // System.out.println(tag.getTagName() + ": " + tag.getDescription());
                }
            }
            
            log.debug("Se extrajo la metadata de la imagen: " + filePath);

            return imageMetadata;
        } catch (Exception e) {
            throw new UtilsApiException(PostUtils.class.getName(), "Error al extraer la metadata de la imagen " + filePath, e);
        }
    }
}
