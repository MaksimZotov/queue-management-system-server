package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "client_in_queue_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInQueueStatusEntity {

    public enum Status {
        RESERVED,
        CONFIRMED,
        LATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}