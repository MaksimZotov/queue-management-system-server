package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "client_in_queue_to_chosen_service")
@IdClass(ClientInQueueToChosenServiceEntity.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInQueueToChosenServiceEntity implements Serializable {

    @Id
    private Long clientId;

    @Id
    private Long serviceId;

    @Id
    private Long queueId;

    @Id
    private Long locationId;
}