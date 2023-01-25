package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "queue_type_in_location")
@IdClass(QueueTypeInLocationEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueTypeInLocationEntity implements Serializable {

    @Id
    private Long queueTypeId;

    @Id
    private Long locationId;
}