package com.maksimzotov.queuemanagementsystemserver.model.location;

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
        Integer orderNumber;
    }

    @Data
    @AllArgsConstructor
    public static class Client {
        Long id;
        Integer code;
        Date waitTimestamp;
        List<Service> allServices;
        Queue queue;
        List<Service> servicesInQueue;
    }

    Long locationId;

    List<Client> clients;

    public static LocationState toModel(
            Long locationId,

            List<ClientEntity> clientEntities,

            List<ServiceEntity> serviceEntities,
            List<ClientToChosenServiceEntity> clientToChosenServiceEntities,

            List<QueueEntity> queueEntities,
            List<ClientInQueueToChosenServiceEntity> clientInQueueToChosenServiceEntities
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
                            List<Service> servicesInQueue = getServicesInQueue(clientEntity, serviceEntities, clientInQueueToChosenServiceEntities);
                            return new Client(
                                    clientEntity.getId(),
                                    code,
                                    waitTimestamp,
                                    allServices,
                                    queue,
                                    servicesInQueue
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

    private static List<Service> getServicesInQueue(
            ClientEntity clientEntity,
            List<ServiceEntity> serviceEntities,
            List<ClientInQueueToChosenServiceEntity> clientInQueueToChosenServiceEntities
    ) {
        return clientInQueueToChosenServiceEntities
                .stream()
                .filter(clientInQueueToChosenServiceEntity ->
                        Objects.equals(
                                clientInQueueToChosenServiceEntity.getClientId(),
                                clientEntity.getId()
                        )
                )
                .map(clientInQueueToChosenServiceEntity ->
                        serviceEntities
                                .stream()
                                .filter(serviceEntity ->
                                        Objects.equals(
                                                serviceEntity.getId(),
                                                clientInQueueToChosenServiceEntity.getServiceId()
                                        )
                                )
                                .map(serviceEntity -> new Service(
                                        serviceEntity.getId(),
                                        serviceEntity.getName(),
                                        0
                                ))
                                .findFirst()
                                .get()
                )
                .toList();
    }
}
