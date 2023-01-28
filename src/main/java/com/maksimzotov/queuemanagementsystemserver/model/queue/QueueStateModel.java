package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QueueStateModel {
    private Long id;
    private Long locationId;
    private String name;
    private String description;
    private List<ClientInQueue> clients;
    @JsonProperty("owner_username")
    private String ownerUsername;
    private Boolean paused;
}
