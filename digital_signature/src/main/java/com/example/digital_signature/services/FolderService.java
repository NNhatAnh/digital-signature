package com.example.digital_signature.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.digital_signature.model.FolderModel;
import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.FolderRepo;

@Service
public class FolderService {
    @Autowired
    private FolderRepo folderRepo;

    public String create(String name, UserModel userID) {
        try {
            FolderModel newFolder = new FolderModel();
            newFolder.setName(name);
            newFolder.setUser(userID);

            folderRepo.save(newFolder);
            return "Create success";
        } catch (Exception e) {
            throw new RuntimeException("Create fail: "+e);
        }
    }
}
