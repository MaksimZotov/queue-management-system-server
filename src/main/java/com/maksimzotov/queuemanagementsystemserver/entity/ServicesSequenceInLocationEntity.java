package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "services_sequence_in_location")
@IdClass(ServicesSequenceInLocationEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicesSequenceInLocationEntity implements Serializable {

    @Id
    private Long servicesSequenceId;

    @Id
    private Long locationId;
}