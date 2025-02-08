package com.example.digital_signature.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.digital_signature.model.FolderModel;
import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.FolderRepo;
import com.example.digital_signature.repository.UserRepo;
import com.example.digital_signature.services.FolderService;

@RestController
@RequestMapping("/folders")
public class FolderController {
    @Autowired
    private FolderService folderService;

    @Autowired
    private FolderRepo folderRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam String name, @RequestParam int userID) {
        UserModel user = userRepo.findByID(userID);
        System.out.println("user: "+user);
        String response = folderService.create(name, user);
        return ResponseEntity.ok(response);
    } 

    @GetMapping("/list")
    public List<FolderModel> listAll() {
        List<FolderModel> list = folderRepo.findAll();
        return list;
    }
}
