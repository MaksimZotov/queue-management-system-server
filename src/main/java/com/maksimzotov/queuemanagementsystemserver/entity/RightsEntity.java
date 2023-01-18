package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "rights")
@IdClass(RightsEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RightsEntity implements Serializable {

    @Id
    private Long locationId;

    @Id
    private String email;
}
