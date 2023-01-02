package com.maksimzotov.queuemanagementsystemserver.model.queue;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QueueState {
    private Long id;
    private String name;
    private String description;
    private List<ClientInQueue> clients;
}
