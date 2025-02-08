package com.example.digital_signature.services;

import java.security.KeyPair;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.UserRepo;
import com.example.digital_signature.util.KeyPairUtil;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    private final KeyPairUtil keyPairUtil = new KeyPairUtil();
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserModel signUp(String username, String password) {
        if (userRepo.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }

        String PasswordEncode = passwordEncoder.encode(password);

        KeyPair keyPair = keyPairUtil.generateKeyPair();
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        UserModel user = new UserModel();
        user.setUsername(username);
        user.setPassword(PasswordEncode);
        user.setPrivateKey(privateKey);
        user.setPublicKey(publicKey);

        return userRepo.save(user);
    }

    public UserModel login(String username, String password) {
        UserModel user = userRepo.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }
}
