package com.coresaken.mcserverlist.service;

import com.coresaken.mcserverlist.data.response.Response;
import com.coresaken.mcserverlist.database.model.server.Server;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BannerFileService {
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 4 * 1024 * 1024;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/banners/";

    public static ResponseEntity<Response> upload(MultipartFile file) {
        if (file.isEmpty()) {
            return Response.badRequest(1, "Akceptujemy jedynie rozszerzenia jpeg, png, i gif");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return Response.badRequest(2, "Akceptujemy jedynie rozszerzenia jpeg, png, i gif");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return Response.badRequest(3,"Rozmiar pliku jest za duży");
        }

        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String newFilename = uuid + fileExtension;

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            file.transferTo(new File(UPLOAD_DIR + newFilename));
        } catch (IOException e) {
            e.printStackTrace();
            return Response.badRequest(4, "Pojawił się błąd #4124. Spróbuj ponownie później lub skontaktuj się z nami");
        }

        return Response.ok("/uploads/banners/"+newFilename);
    }

    public static void remove(String fileName){
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName.split("/")[3]).normalize();
            File file = filePath.toFile();

            if (file.exists()) {
                file.delete();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
