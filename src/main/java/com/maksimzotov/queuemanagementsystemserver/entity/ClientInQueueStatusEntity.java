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

    public ClientInQueueStatusEntity(
            QueueEntity queue,
            String clientEmail,
            String clientFirstName,
            String clientLastName,
            Integer clientOrderNumber
    ) {
        this.queue = queue;
        this.clientEmail = clientEmail;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.clientOrderNumber = clientOrderNumber;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "queue_id",  referencedColumnName="id")
    private QueueEntity queue;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_first_name")
    private String clientFirstName;

    @Column(name = "client_last_name")
    private String clientLastName;

    @Column(name = "client_order_number")
    private Integer clientOrderNumber;
}