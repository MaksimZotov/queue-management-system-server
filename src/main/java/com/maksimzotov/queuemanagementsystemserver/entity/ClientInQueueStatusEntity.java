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

    public static String RESERVED = "RESERVED";
    public static String IN_QUEUE = "IN_QUEUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}