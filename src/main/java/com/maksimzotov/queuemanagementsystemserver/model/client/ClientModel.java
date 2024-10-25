package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ClientModel {
    Long id;
    String phone;
    Integer code;
    String status;

    public static ClientModel toModel(ClientEntity clientEntity) {
        return new ClientModel(
                clientEntity.getId(),
                clientEntity.getPhone(),
                clientEntity.getCode(),
                clientEntity.getStatus()
        );
    }
}
