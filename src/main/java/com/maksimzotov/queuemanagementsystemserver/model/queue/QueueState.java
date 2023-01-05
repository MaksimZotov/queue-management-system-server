package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@AllArgsConstructor
@Jacksonized
public class QueueState {
    private Long id;
    private String name;
    private String description;
    private List<ClientInQueue> clients;
    @JsonProperty("owner_username")
    private String ownerUsername;
}
