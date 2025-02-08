package com.example.digital_signature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.digital_signature.model.SignatureModel;
import com.example.digital_signature.model.UserModel;


@Repository
public interface SignatureRepo extends JpaRepository<SignatureModel, Integer>{
    SignatureModel findByUser(UserModel user);
}