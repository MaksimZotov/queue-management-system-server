package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "client_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientCodeEntity implements Serializable {

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

    private String code;
}
