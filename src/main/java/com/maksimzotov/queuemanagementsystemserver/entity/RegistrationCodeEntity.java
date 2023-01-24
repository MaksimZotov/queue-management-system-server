package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "registration_code")
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCodeEntity {

    @Id
    private String username;

    private String code;
}