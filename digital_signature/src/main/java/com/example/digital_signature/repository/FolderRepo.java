package com.example.digital_signature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.digital_signature.model.FolderModel;

@Repository
public interface FolderRepo extends JpaRepository<FolderModel, Integer>{
    
}
