package com.example.digital_signature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.digital_signature.model.UserModel;


@Repository
public interface UserRepo extends JpaRepository<UserModel, Integer>{
    UserModel findByUsername(String username);
    UserModel findByID(int iD);
}
