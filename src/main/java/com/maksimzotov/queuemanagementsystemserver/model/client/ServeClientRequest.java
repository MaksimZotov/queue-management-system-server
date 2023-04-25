package com.maksimzotov.queuemanagementsystemserver.model.client;

import lombok.Value;

import java.util.List;

@Value
public class ServeClientRequest {
    List<Long> services;
}
