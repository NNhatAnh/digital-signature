package com.example.digital_signature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.digital_signature.model.DocumentModel;
import com.example.digital_signature.model.UserModel;

import java.util.List;


@Repository
public interface DocumentRepo extends JpaRepository<DocumentModel, Integer>{
    List<DocumentModel> findByUser(UserModel user);
    DocumentModel findByID(int iD);
}
