package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.Constants;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.client.ServeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final AccountService accountService;
    private final LocationService locationService;
    private final RightsService rightsService;
    private final MailService mailService;
    private final DelayedJobService delayedJobService;
    private final CleanerService cleanerService;
    private final ClientRepo clientRepo;
    private final QueueRepo queueRepo;
    private final LocationRepo locationRepo;
    private final ServiceRepo serviceRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            AccountService accountService,
            LocationService locationService,
            RightsService rightsService,
            MailService mailService,
            @Lazy QueueService queueService,
            DelayedJobService delayedJobService,
            CleanerService cleanerService,
            ClientRepo clientRepo,
            QueueRepo queueRepo,
            LocationRepo locationRepo,
            ServiceRepo serviceRepo,
            ServicesSequenceRepo servicesSequenceRepo,
            ServiceInServicesSequenceRepo serviceInServicesSequenceRepo,
            ClientToChosenServiceRepo clientToChosenServiceRepo,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.accountService = accountService;
        this.locationService = locationService;
        this.rightsService = rightsService;
        this.mailService = mailService;
        this.delayedJobService = delayedJobService;
        this.cleanerService = cleanerService;
        this.clientRepo = clientRepo;
        this.queueRepo = queueRepo;
        this.locationRepo = locationRepo;
        this.serviceRepo = serviceRepo;
        this.servicesSequenceRepo = servicesSequenceRepo;
        this.serviceInServicesSequenceRepo = serviceInServicesSequenceRepo;
        this.clientToChosenServiceRepo = clientToChosenServiceRepo;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public void addClient(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException {
        Map<Long, Integer> serviceIdsToOrderNumbers = checkAddClientRequest(localizer, locationId, addClientRequest);
        createClient(localizer, locationId, addClientRequest, serviceIdsToOrderNumbers);
    }

    @Override
    public void callClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queue.get();
        if (queueEntity.getClientId() != null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_ASSIGNED_TO_QUEUE));
        }

        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }

        ClientEntity clientEntity = client.get();
        Long locationId = queueEntity.getLocationId();

        queueEntity.setClientId(clientId);
        queueRepo.save(queueEntity);

        mailService.send(
                clientEntity.getEmail(),
                localizer.getMessage(Message.YOUR_STATUS_IN_QUEUE),
                localizer.getMessageForClientCheckStatus(
                        queueEntity.getName(),
                        clientEntity.getCode().toString(),
                        getLinkForClient(localizer, clientEntity, locationId)
                )
        );

        locationService.updateLocationState(locationId);
    }

    @Override
    public void returnClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queue.get();
        queueEntity.setClientId(null);
        queueRepo.save(queueEntity);

        locationService.updateLocationState(queueEntity.getLocationId());
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);
        return QueueStateForClient.toModel(clientEntity);
    }

    @Override
    public QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);
        if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.CONFIRMED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_CONFIRMED));
        }

        List<ClientEntity> clientEntities = clientRepo.findAllByLocationId(clientEntity.getLocationId());

        clientEntity.setStatus(ClientStatusEntity.Status.CONFIRMED.name());
        clientEntity.setWaitTimestamp(new Date());
        clientEntity.setCode(CodeGenerator.generateCodeInLocation(clientEntities.stream().map(ClientEntity::getCode).toList()));

        clientRepo.save(clientEntity);
        locationService.updateLocationState(clientEntity.getLocationId());

        return getQueueStateForClient(localizer, clientId, accessKey);
    }

    @Override
    public QueueStateForClient leaveByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        checkAccessKey(localizer, clientId, accessKey);
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientRepo.deleteById(clientId);
        Optional<QueueEntity> queue = queueRepo.findByClientId(clientId);
        if (queue.isPresent()) {
            QueueEntity queueEntity = queue.get();
            queueEntity.setClientId(null);
            queueRepo.save(queueEntity);
        }
        locationService.updateLocationState(client.get().getLocationId());
        return getQueueStateForClient(localizer, clientId, accessKey);
    }

    @Override
    public void serveClientInQueueByEmployee(Localizer localizer, String accessToken, ServeClientRequest serveClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, serveClientRequest.getQueueId());

        Optional<Integer> minOrderNumber = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(serveClientRequest.getClientId())
                .stream()
                .map(ClientToChosenServiceEntity::getOrderNumber)
                .min(Integer::compareTo);

        if (minOrderNumber.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }

        Integer min = minOrderNumber.get();

        for (Long serviceId : serveClientRequest.getServices()) {
            Optional<ClientToChosenServiceEntity> clientToChosenService = clientToChosenServiceRepo.findByPrimaryKeyClientIdAndPrimaryKeyServiceId(
                    serveClientRequest.getClientId(),
                    serviceId
            );
            if (clientToChosenService.isEmpty()) {
                throw new DescriptionException(localizer.getMessage(Message.INCORRECT_SERVICES));
            }
            ClientToChosenServiceEntity clientToChosenServiceEntity = clientToChosenService.get();
            if (!Objects.equals(clientToChosenServiceEntity.getOrderNumber(), min)) {
                throw new DescriptionException(localizer.getMessage(Message.INCORRECT_SERVICES));
            }
            clientToChosenServiceRepo.delete(clientToChosenServiceEntity);
        }

        Optional<QueueEntity> queue = queueRepo.findById(serveClientRequest.getQueueId());
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        queueEntity.setClientId(null);
        queueRepo.save(queueEntity);

        if (!clientToChosenServiceRepo.existsByPrimaryKeyClientId(serveClientRequest.getClientId())) {
            clientRepo.deleteById(serveClientRequest.getClientId());
        } else {
            Optional<ClientEntity> client = clientRepo.findById(serveClientRequest.getClientId());
            if (client.isPresent()) {
                ClientEntity clientEntity = client.get();
                clientEntity.setWaitTimestamp(new Date());
                clientRepo.save(clientEntity);
            }
        }

        locationService.updateLocationState(queueEntity.getLocationId());
    }

    @Override
    public void notifyClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (clientEntity.getEmail() == null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_HAVE_EMAIL));
        }
        mailService.send(clientEntity.getEmail(), localizer.getMessage(Message.QUEUE), localizer.getMessage(Message.PLEASE_GO_TO_SERVICE));
    }

    @Override
    public void deleteClientInLocation(Localizer localizer, String accessToken, Long locationId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId);
        Optional<QueueEntity> queue = queueRepo.findByClientId(clientId);
        if (queue.isPresent()) {
            QueueEntity queueEntity = queue.get();
            queueEntity.setClientId(null);
            queueRepo.save(queueEntity);
        }
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientRepo.deleteById(clientId);
        locationService.updateLocationState(locationId);
    }

    private void checkRightsInQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountEmail, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
    }

    private Map<Long, Integer> checkAddClientRequest(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException {
        boolean servicesChosen = addClientRequest.getServiceIds() != null && !addClientRequest.getServiceIds().isEmpty();
        boolean servicesSequenceChosen = addClientRequest.getServicesSequenceId() != null;
        if (servicesChosen == servicesSequenceChosen) {
            throw new DescriptionException(localizer.getMessage(Message.INCORRECT_REQUEST));
        }

        if (!EmailChecker.emailMatches(addClientRequest.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
        }

        Map<Long, Integer> serviceIdsToOrderNumbers = new HashMap<>();
        if (servicesChosen) {
            List<Long> serviceIds = addClientRequest.getServiceIds();
            for (Long serviceId : serviceIds) {
                if (!serviceRepo.existsByIdAndLocationId(serviceId, locationId)) {
                    throw new DescriptionException(localizer.getMessage(Message.ONE_OR_MORE_OF_CHOSEN_SERVICES_DO_NOT_EXIST_IN_LOCATION));
                }
                serviceIdsToOrderNumbers.put(serviceId, 1);
            }
        } else {
            Long servicesSequenceId = addClientRequest.getServicesSequenceId();
            if (!servicesSequenceRepo.existsByIdAndLocationId(servicesSequenceId, locationId)) {
                throw new DescriptionException(localizer.getMessage(Message.CHOSEN_SERVICES_SEQUENCE_DOES_NOT_EXIST_IN_LOCATION));
            }
            List<ServiceInServicesSequenceEntity> servicesInServicesSequenceEntities =
                    serviceInServicesSequenceRepo.findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(
                            servicesSequenceId
                    );
            for (ServiceInServicesSequenceEntity servicesSequenceEntity : servicesInServicesSequenceEntities) {
                serviceIdsToOrderNumbers.put(servicesSequenceEntity.getPrimaryKey().getServiceId(), servicesSequenceEntity.getOrderNumber());
            }
        }

        return serviceIdsToOrderNumbers;
    }

    private ClientEntity checkAccessKey(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (!Objects.equals(clientEntity.getAccessKey(), accessKey)) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_ACCESS_KEY));
        }
        return clientEntity;
    }

    private String getLinkForClient(Localizer localizer, ClientEntity clientEntity, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return Constants.CLIENT_URL +
                "/client?client_id=" +
                clientEntity.getId() +
                "&access_key=" +
                clientEntity.getAccessKey();
    }

    private void createClient(Localizer localizer, Long locationId, AddClientRequst addClientRequest, Map<Long, Integer> serviceIdsToOrderNumbers) throws DescriptionException {
        if (addClientRequest.getEmail() != null && clientRepo.findByEmail(addClientRequest.getEmail()).isPresent()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_WITH_THIS_EMAIL_ALREADY_EXIST));
        }

        ClientEntity clientEntity = clientRepo.save(
                new ClientEntity(
                        null,
                        locationId,
                        addClientRequest.getEmail(),
                        null,
                        CodeGenerator.generateAccessKey(),
                        ClientStatusEntity.Status.RESERVED.name(),
                        null
                )
        );

        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : serviceIdsToOrderNumbers.entrySet()) {
            clientToChosenServiceRepo.save(
                    new ClientToChosenServiceEntity(
                            new ClientToChosenServiceEntity.PrimaryKey(
                                    clientEntity.getId(),
                                    serviceIdToOrderNumber.getKey(),
                                    locationId
                            ),
                            serviceIdToOrderNumber.getValue()
                    )
            );
        }

        delayedJobService.schedule(
                () -> cleanerService.deleteNonConfirmedClient(clientEntity.getId(), addClientRequest.getEmail()),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        mailService.send(
                addClientRequest.getEmail(),
                localizer.getMessage(Message.CONFIRMATION_OF_CONNECTION),
                localizer.getMessageForClientConfirmation(getLinkForClient(localizer, clientEntity, locationId))
        );
    }
}
