// package com.example.digital_signature.controller;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.digital_signature.services.DigitalService;
// import com.example.digital_signature.services.UserService;

// @RestController
// public class DigitalController {
//     private DigitalService digitalService;
//     private UserService userService;    

//     @PostMapping("/login")
//     public ResponseEntity<String> login(@RequestBody String data) {
//         String Signdata = userService.login(data);
//         return ResponseEntity.ok(Signdata);
//     }

//     @PostMapping("/signup")
//     public ResponseEntity<String> signUp(@RequestBody String data) {
//         String SignData = userService.signUp();
//         return ResponseEntity.ok(SignData);
//     }
// }
