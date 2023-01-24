package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "client_chosen_service")
@NoArgsConstructor
@AllArgsConstructor
public class ClientChosenServiceEntity implements Serializable {

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private Long clientId;
        private Long serviceId;
    }

    @EmbeddedId
    private PrimaryKey primaryKey;

    private Integer orderNumber;

    private Boolean queueIsKnown;
}
