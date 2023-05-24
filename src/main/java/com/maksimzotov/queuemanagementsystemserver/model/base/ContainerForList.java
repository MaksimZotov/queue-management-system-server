package com.maksimzotov.queuemanagementsystemserver.model.base;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
public class ContainerForList<T> {
    List<T> results;
}
