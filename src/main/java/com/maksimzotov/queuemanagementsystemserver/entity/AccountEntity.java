package com.maksimzotov.queuemanagementsystemserver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Entity(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    public AccountEntity(
            String username,
            String email,
            String firstName,
            String lastName,
            String password,
            String code
    ) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;

        RegistrationCodeEntity registrationCode = new RegistrationCodeEntity();
        registrationCode.setAccount(this);
        registrationCode.setCode(code);
        this.registrationCode = registrationCode;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String password;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonBackReference
    private RegistrationCodeEntity registrationCode;

    public void deleteRegistrationCode() {
        registrationCode.setAccount(null);
        registrationCode = null;
    }
}
