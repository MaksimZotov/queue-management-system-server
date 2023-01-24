package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "service_in_queue")
@IdClass(ServiceInQueueEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInQueueEntity implements Serializable {

    @Id
    private Long serviceId;

    @Id
    private Long queueId;
}