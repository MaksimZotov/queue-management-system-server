package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "registration_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCodeEntity {

    @Id
    private String email;

    private Integer code;
}