package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "rights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RightsEntity implements Serializable {

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private Long locationId;
        private String email;
    }

    @EmbeddedId
    private PrimaryKey primaryKey;

    private String status;
}
