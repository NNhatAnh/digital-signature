package com.example.digital_signature.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_signature.model.DocumentModel;
import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.DocumentRepo;
import com.example.digital_signature.repository.UserRepo;
import com.example.digital_signature.services.DocumentService;

@RestController
@RequestMapping("/files")
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepo documentRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/listFile")
    public List<DocumentModel> listByUser(@RequestParam int userID) {
        UserModel user = userRepo.findByID(userID);
        List<DocumentModel> list = documentRepo.findByUser(user);
        return list;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploads(@RequestParam("file") MultipartFile file, @RequestParam("userID") int userID) {
        try {
            UserModel user = userRepo.findByID(userID);
            String filePath = documentService.saveFile(file, user);
            System.out.println("File: " + file);
            return ResponseEntity.ok(filePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/{fileID}")
    public ResponseEntity<?> openFile(@PathVariable int fileID, @RequestParam int userID) {
        try {
            UserModel user = userRepo.findById(userID)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            DocumentModel document = documentRepo.findById(fileID)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found"));

            boolean isVerify = documentService.verifyFileHash(document, user);

            if (isVerify) {
                Path filePath = Paths.get(document.getDocumentPath());
                byte[] fileContent = Files.readAllBytes(filePath);

                Map<String, Object> response = new HashMap<>();
                response.put("fileContent", Base64.getEncoder().encodeToString(fileContent));
                response.put("contentType", MediaType.APPLICATION_PDF_VALUE);
                response.put("signedBy", document.getSignedBy());

                return ResponseEntity.ok(response);

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("The file has been changed");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }
}
