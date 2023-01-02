package com.maksimzotov.queuemanagementsystemserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntity {

    public QueueEntity(
            String name,
            String description,
            LocationEntity location
    ) {
        this.name = name;
        this.description = description;
        this.location = location;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id",  referencedColumnName="id")
    private LocationEntity location;

    private String name;

    private String description;
}
