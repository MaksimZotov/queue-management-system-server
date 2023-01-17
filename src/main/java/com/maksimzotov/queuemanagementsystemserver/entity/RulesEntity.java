package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "rules")
@IdClass(RulesEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RulesEntity implements Serializable {

    @Id
    private Long locationId;

    @Id
    private String email;
}
