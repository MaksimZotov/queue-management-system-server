package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity {

    public LocationEntity(
            String name,
            String description,
            AccountEntity owner
    ) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_username",  referencedColumnName="username")
    private AccountEntity owner;

    private String name;

    private String description;
}
