package com.example.digital_signature.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.digital_signature.services.UserService;
import com.example.digital_signature.util.JwtUtil;
import com.example.digital_signature.model.UserModel;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserModel> signUp(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        UserModel newUser = userService.signUp(username, password);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        UserModel user = userService.login(username, password);

        JwtUtil jwtUtil = new JwtUtil();
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(token);
    }
}
