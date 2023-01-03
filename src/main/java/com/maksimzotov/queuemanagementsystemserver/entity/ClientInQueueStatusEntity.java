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
            String email,
            String firstName,
            String lastName,
            Integer orderNumber
    ) {
        this.queue = queue;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.orderNumber = orderNumber;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "queue_id",  referencedColumnName="id")
    private QueueEntity queue;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "order_number")
    private Integer orderNumber;
}