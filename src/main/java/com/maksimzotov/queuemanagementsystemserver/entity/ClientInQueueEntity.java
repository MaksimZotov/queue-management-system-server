package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "client_in_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInQueueEntity {

    @Id
    private Long clientId;

    private Long queueId;
}