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

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private Long queueId;
        private String email;
    }

    @EmbeddedId
    private PrimaryKey primaryKey;

    private String firstName;

    private String lastName;

    private Integer orderNumber;

    private String accessKey;

    private String status;
}