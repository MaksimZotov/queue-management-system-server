package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "service_in_history_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInHistoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long historyItemId;

    private Long serviceId;
}
