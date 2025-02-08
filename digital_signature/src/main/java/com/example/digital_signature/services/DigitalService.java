// package com.example.digital_signature.services;

// import org.springframework.stereotype.Service;

// import com.example.digital_signature.util.DigitalSignatureUtil;
// import com.example.digital_signature.util.KeyPairUtil;

// import java.util.Base64;

// @Service
// public class DigitalService {
//     private final KeyPairUtil keyPairUtil = new KeyPairUtil();

//     public String signData(String data) {
//         try {
//             byte[] signature = DigitalSignatureUtil.signData(data.getBytes(), keyPairUtil.getPrivateKey());
//             return Base64.getEncoder().encodeToString(signature);
//         } catch (Exception e) {
//             throw new RuntimeException("Error while signing data", e);
//         }
//     }

//     public boolean verifyData(String data, String signature) {
//         try {
//             byte[] decodedSignature = Base64.getDecoder().decode(signature);
//             return DigitalSignatureUtil.verifySignature(data.getBytes(), decodedSignature, keyPairUtil.getPublicKey());
//         } catch (Exception e) {
//             throw new RuntimeException("Error while verifying signature", e);
//         }
//     }
// }
