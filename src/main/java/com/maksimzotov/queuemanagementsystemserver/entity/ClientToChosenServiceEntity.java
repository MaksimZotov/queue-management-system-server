package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "client_to_chosen_service")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientToChosenServiceEntity implements Serializable {

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private Long clientId;
        private Long serviceId;
        private Long locationId;
    }

    @EmbeddedId
    private PrimaryKey primaryKey;

    private Integer orderNumber;
}
