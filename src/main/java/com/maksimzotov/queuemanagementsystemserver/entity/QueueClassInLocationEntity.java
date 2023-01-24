package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "queue_class_in_location")
@IdClass(QueueClassInLocationEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueClassInLocationEntity implements Serializable {

    @Id
    private Long queueClassId;

    @Id
    private Long locationId;
}