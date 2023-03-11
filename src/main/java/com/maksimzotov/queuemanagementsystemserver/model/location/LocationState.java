package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@AllArgsConstructor
public class LocationState {
    @Data
    @AllArgsConstructor
    public static class Queue {
        Long id;
        String name;
    }

    @Data
    @AllArgsConstructor
    public static class Service {
        Long id;
        String name;
        @JsonProperty("order_number")
        Integer orderNumber;
    }

    @Data
    @AllArgsConstructor
    public static class Client {
        Long id;
        Integer code;
        @JsonProperty("wait_timestamp")
        Date waitTimestamp;
        List<Service> services;
        Queue queue;
    }

    Long id;

    List<Client> clients;

    public static LocationState toModel(
            Long locationId,
            List<ClientEntity> clientEntities,
            List<ServiceEntity> serviceEntities,
            List<ClientToChosenServiceEntity> clientToChosenServiceEntities,
            List<QueueEntity> queueEntities
    ) {
        return new LocationState(
                locationId,
                clientEntities
                        .stream()
                        .map(clientEntity -> {
                            Integer code = getCode(clientEntity);
                            Date waitTimestamp = getWaitTime(clientEntity);
                            List<Service> allServices = getAllServices(clientEntity, serviceEntities, clientToChosenServiceEntities);
                            Queue queue = getQueue(clientEntity, queueEntities);
                            return new Client(
                                    clientEntity.getId(),
                                    code,
                                    waitTimestamp,
                                    allServices,
                                    queue
                            );
                        })
                        .toList()
        );
    }

    private static Integer getCode(ClientEntity clientEntity) {
        return clientEntity.getCode();
    }

    private static Date getWaitTime(ClientEntity clientEntity) {
        return clientEntity.getWaitTimestamp();
    }

    private static List<Service> getAllServices(
            ClientEntity clientEntity,
            List<ServiceEntity> serviceEntities,
            List<ClientToChosenServiceEntity> clientToChosenServiceEntities
    ) {
        return clientToChosenServiceEntities
                .stream()
                .filter(clientToChosenServiceEntity ->
                        Objects.equals(
                                clientToChosenServiceEntity.getPrimaryKey().getClientId(),
                                clientEntity.getId()
                        )
                )
                .map(clientToChosenServiceEntity ->
                        serviceEntities
                                .stream()
                                .filter(serviceEntity ->
                                        Objects.equals(
                                                serviceEntity.getId(),
                                                clientToChosenServiceEntity.getPrimaryKey().getServiceId()
                                        )
                                )
                                .map(serviceEntity -> new Service(
                                        serviceEntity.getId(),
                                        serviceEntity.getName(),
                                        clientToChosenServiceEntity.getOrderNumber()
                                ))
                                .findFirst()
                                .get()
                )
                .toList();
    }

    private static Queue getQueue(
            ClientEntity clientEntity,
            List<QueueEntity> queueEntities
    ) {
        Optional<Queue> queue = queueEntities
                .stream()
                .filter(queueEntity ->
                        Objects.equals(
                                queueEntity.getClientId(),
                                clientEntity.getId()
                        )
                )
                .map(queueEntity -> new Queue(
                        queueEntity.getId(),
                        queueEntity.getName()
                ))
                .findFirst();

        if (queue.isEmpty()) {
            return null;
        } else  {
            return queue.get();
        }
    }
}
