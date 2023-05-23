package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

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
        String phone;
        @JsonProperty("wait_timestamp")
        Date waitTimestamp;
        @JsonProperty("total_timestamp")
        Date totalTimestamp;
        List<Service> services;
        Queue queue;

        public static Client toModel(
                ClientEntity clientEntity,
                List<ServiceEntity> serviceEntities,
                List<ClientToChosenServiceEntity> clientToChosenServiceEntities,
                QueueEntity queueEntity
        ) {
            Integer code = getCode(clientEntity);
            Date waitTimestamp = getWaitTime(clientEntity);
            Date totalTimestamp = getTotalTime(clientEntity);
            List<Service> services = mapServices(serviceEntities, clientToChosenServiceEntities);
            Queue queue = mapQueue(queueEntity);
            return new Client(
                    clientEntity.getId(),
                    code,
                    clientEntity.getPhone(),
                    waitTimestamp,
                    totalTimestamp,
                    services,
                    queue
            );
        }

        public static Client toModelWithAllQueuesInLocation(
                ClientEntity clientEntity,
                List<ServiceEntity> serviceEntities,
                List<ClientToChosenServiceEntity> clientToChosenServiceEntities,
                List<QueueEntity> queueEntities
        ) {
            Integer code = getCode(clientEntity);
            Date waitTimestamp = getWaitTime(clientEntity);
            Date totalTimestamp = getTotalTime(clientEntity);
            List<Service> services = mapServices(serviceEntities, clientToChosenServiceEntities);
            Queue queue = getQueue(clientEntity, queueEntities);
            return new Client(
                    clientEntity.getId(),
                    code,
                    clientEntity.getPhone(),
                    waitTimestamp,
                    totalTimestamp,
                    services,
                    queue
            );
        }

        private static Integer getCode(ClientEntity clientEntity) {
            return clientEntity.getCode();
        }

        private static Date getWaitTime(ClientEntity clientEntity) {
            return clientEntity.getWaitTimestamp();
        }

        private static Date getTotalTime(ClientEntity clientEntity) {
            return clientEntity.getTotalTimestamp();
        }

        private static List<Service> mapServices(
                List<ServiceEntity> serviceEntities,
                List<ClientToChosenServiceEntity> clientToChosenServiceEntities
        ) {
            return clientToChosenServiceEntities
                    .stream()
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

        private static Queue mapQueue(
                QueueEntity queueEntity
        ) {
            if (queueEntity == null) {
                return null;
            }
            return new Queue(
                    queueEntity.getId(),
                    queueEntity.getName()
            );
        }
    }

    Long id;

    List<Client> clients;

    public static LocationState toModel(
            Long locationId,
            List<ServiceEntity> serviceEntities,
            List<QueueEntity> queueEntities,
            Map<ClientEntity, List<ClientToChosenServiceEntity>> clientToChosenServices
    ) {
        List<Client> clients = clientToChosenServices.keySet()
                .stream()
                .filter(clientEntity -> clientEntity.getCode() != null)
                .map(clientEntity ->
                        Client.toModelWithAllQueuesInLocation(
                                clientEntity,
                                serviceEntities,
                                clientToChosenServices.get(clientEntity),
                                queueEntities
                        )
                )
                .sorted(Comparator.comparing(Client::getCode))
                .toList();

        return new LocationState(
                locationId,
                clients
        );
    }
}
