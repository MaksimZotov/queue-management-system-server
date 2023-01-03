package com.maksimzotov.queuemanagementsystemserver.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "registration_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCodeEntity {

    @Id
    private String username;

    private String code;
}