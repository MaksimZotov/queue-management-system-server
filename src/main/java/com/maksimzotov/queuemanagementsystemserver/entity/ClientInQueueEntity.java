package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "client_in_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long queueId;

    private String email;

    private String firstName;

    private String lastName;

    private Integer orderNumber;

    private Integer publicCode;

    private String accessKey;

    private String status;
}