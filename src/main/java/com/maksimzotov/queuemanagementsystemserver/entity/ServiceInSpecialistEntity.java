package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "service_in_specialist")
@IdClass(ServiceInSpecialistEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInSpecialistEntity implements Serializable {

    @Id
    private Long serviceId;

    @Id
    private Long specialistId;
}