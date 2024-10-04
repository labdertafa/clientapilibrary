package com.laboratorio.apiclient.utiles;

import com.laboratorio.clientapilibrary.utils.ElementoPost;
import com.laboratorio.clientapilibrary.utils.ImageMetadata;
import com.laboratorio.clientapilibrary.utils.PostUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Rafael
 * @version 1.0
 * @created 07/09/2024
 * @updated 04/10/2024
 */
public class UtilesTest {
    @Test
    public void extraerElementosPost() {
        String texto = "Creando una publicación de prueba desde postman y con el link:\n\nhttps://laboratoriorafa.mooo.com/\n\na ver que tal #siguemeytesigo #Followback\n\ny un pedazo\nal final.";
        
        List<ElementoPost> resultados = PostUtils.extraerElementosPost(texto);
        
        for (ElementoPost ep : resultados) {
            System.out.println(String.format("Elemento: %s - Tipo: %s", ep.getContenido(), ep.getType().name()));
        }
        
        assertEquals(7, resultados.size());
    }
    
    @Test
    public void getUrlMetadata() throws IOException {
        String ulr = "https://laboratoriorafa.mooo.com";
        
        Map<String, String> metadata = PostUtils.getUrlMetadata(ulr);
        
        for (Map.Entry<String, String> dato : metadata.entrySet()) {
            System.out.println(String.format("%s: %s", dato.getKey(), dato.getValue()));
        }
        
        assertTrue(!metadata.isEmpty());
    }
    
    @Test
    public void calculateMD5() {
        String filePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Spa_1950.jpg";
        String md5sum = "MaRQuDADGlcNauJW6IE5Hg==";
        
        // String result = PostUtils.calculateMD5OfCompressedFile(filePath);
        String result = PostUtils.calculateMD5Base64(filePath);
        
        assertEquals(md5sum, result);
    }
    
    @Test
    public void extractImageMetadata() {
        // String filePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Circuit_Aintree_1955.png";
        String filePath = "C:\\Users\\rafa\\Pictures\\Formula_1\\Spa_1950.jpg";
        int width = 583;
        int height  =405;
        
        ImageMetadata imageMetadata = PostUtils.extractImageMetadata(filePath);
        
        assertEquals(width, imageMetadata.getWidth());
        assertEquals(height, imageMetadata.getHeight());
    }
    
    @Test
    public void getYouTubeMetadata() throws IOException {
        String url = "https://www.youtube.com/watch?v=zkSz6gkY2hk&list=PLtdeXn2f7ZbPXR2R0JC0Qc8OURym5-Zze";
        
        Map<String, String> metadata = PostUtils.getUrlMetadata(url);
        
        String image = metadata.get("previmg");
        assertTrue(image != null);
    }
    
    @Test
    public void saveImageUrl() throws IOException {
        String url = "https://www.youtube.com/watch?v=zkSz6gkY2hk&list=PLtdeXn2f7ZbPXR2R0JC0Qc8OURym5-Zze";
        String destination = "./socialimprove/temp/thumbnail.jpg";
        
        Map<String, String> metadata = PostUtils.getUrlMetadata(url);
        String image = metadata.get("previmg");
        assertTrue(image != null);
        if (image == null) {
            return;
        }
        
        PostUtils.downloadImage(image, destination);
        
        assertTrue(true);
    }
}