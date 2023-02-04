package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity(name = "service_in_services_sequence")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInServicesSequenceEntity implements Serializable {

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private Long serviceId;
        private Long servicesSequenceId;
    }

    @EmbeddedId
    private PrimaryKey primaryKey;

    private Integer orderNumber;
}
