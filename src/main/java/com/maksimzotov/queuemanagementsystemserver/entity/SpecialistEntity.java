package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "specialist")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long locationId;

    private String name;

    private String description;
}
