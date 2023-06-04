package com.maksimzotov.queuemanagementsystemserver.model.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServeClientRequest {
    List<Long> services;
}
