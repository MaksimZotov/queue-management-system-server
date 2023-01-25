package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "client_in_queue_to_chosen_service")
@IdClass(ClientInQueueToChosenService.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInQueueToChosenService implements Serializable {

    @Id
    private Long clientInQueueId;

    @Id
    private Long serviceId;
}