package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "rights_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RightsStatusEntity {

    public enum Status {
        EMPLOYEE,
        ADMINISTRATOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}