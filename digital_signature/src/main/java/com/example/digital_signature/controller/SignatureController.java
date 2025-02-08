package com.example.digital_signature.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.digital_signature.model.SignatureModel;
import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.UserRepo;
import com.example.digital_signature.services.SignatureService;

@RestController
@RequestMapping("/signature")
public class SignatureController {
    @Autowired
    private SignatureService signatureService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/saveSign")
    public ResponseEntity<String> saveSign(@RequestParam int userID, @RequestParam String signatureData) {
        try {

            String result = signatureService.saveSignature(userID, signatureData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/storeSign")
    public ResponseEntity<?> storeSign(@RequestParam int userID) {
        UserModel user = userRepo.findByID(userID);

        try {
            SignatureModel signature = signatureService.storeSign(user);
            return ResponseEntity.ok(signature);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while retrieving signature !");
        }
    }

    @PostMapping("/signDocument")
    public ResponseEntity<?> sign(@RequestParam int documentID, @RequestParam int userID,
            @RequestParam String signatureData) {

        try {
            String response = signatureService.signDocument(documentID, userID, signatureData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while sign the document: " + e);
        }
    }
}
