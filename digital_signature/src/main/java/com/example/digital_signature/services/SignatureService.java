package com.example.digital_signature.services;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.digital_signature.model.SignatureModel;
import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.SignatureRepo;
import com.example.digital_signature.repository.UserRepo;

@Service
public class SignatureService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private SignatureRepo signatureRepo;

    public String saveSignature(int userID, String signatureData) {
        try {
            byte[] signatureBytes = Base64.getDecoder().decode(signatureData);
            UserModel user = userRepo.findByID(userID);

            SignatureModel newSignature = new SignatureModel();
            newSignature.setUser(user);
            newSignature.setSignatureData(signatureBytes);
            signatureRepo.save(newSignature);

            return "Signature saved successfully";
        } catch (Exception e) {
            throw new RuntimeException("Error saving signature: " + e.getMessage());
        }
    }

    public SignatureModel storeSign(UserModel user) {
        SignatureModel signatures = signatureRepo.findByUser(user);
        return signatures;
    }

    public String signDocument(int documentID, int userID, String signatureData) {
        try {
            UserModel user = userRepo.findByID(userID);
            return documentService.signDocument(documentID, user, signatureData);
        } catch (Exception e) {
            throw new RuntimeException("Error: "+e);
        }
    }
}
