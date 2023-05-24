package com.maksimzotov.queuemanagementsystemserver.model.client;

import lombok.Data;

import java.util.List;

@Data
public class ServeClientRequest {
    List<Long> services;
}
