package com.maksimzotov.queuemanagementsystemserver.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "registration_code")
@Getter
@Setter
public class RegistrationCodeEntity {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @MapsId
    @JsonManagedReference
    private AccountEntity account;

    private String code;
}