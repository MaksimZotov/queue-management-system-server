package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.extension.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.client.ChangeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ClientModel;
import com.maksimzotov.queuemanagementsystemserver.model.client.CreateClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ServeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueModel;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(PostgreSQLExtension.class)
@TestPropertySource(
        properties = {"spring.config.location=classpath:application-tests.yml"}
)
@DirtiesContext
public class ClientScopeTests {

    private static final String PASSWORD = "12345678";
    private static final String EMAIL = "zotovm256@gmail.com";
    private static final Long NON_EXISTING_ID = 1000L;

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RightsService rightsService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private SpecialistService specialistService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private ServicesSequenceService servicesSequenceService;

    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private LocationRepo locationRepo;
    @Autowired
    private QueueRepo queueRepo;
    @Autowired
    private SpecialistRepo specialistRepo;
    @Autowired
    private ServiceInSpecialistRepo serviceInSpecialistRepo;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private ClientToChosenServiceRepo clientToChosenServiceRepo;
    @Autowired
    private ServicesSequenceRepo servicesSequenceRepo;
    @Autowired
    private ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokensResponse tokens;
    private LocationModel locationModel;
    private ServiceModel firstServiceModel;
    private ServiceModel secondServiceModel;
    private QueueModel firstQueueModel;
    private QueueModel secondQueueModel;
    private ClientModel confirmedClientModel;
    private ClientModel nonConfirmedClientModel;

    @BeforeEach
    void beforeEach() {
        clientToChosenServiceRepo.deleteAll();
        clientRepo.deleteAll();
        queueRepo.deleteAll();
        serviceInSpecialistRepo.deleteAll();
        specialistRepo.deleteAll();
        serviceInServicesSequenceRepo.deleteAll();
        servicesSequenceRepo.deleteAll();
        serviceRepo.deleteAll();
        locationRepo.deleteAll();
        accountRepo.deleteAll();

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );

        accountRepo.save(
                new AccountEntity(
                        null,
                        EMAIL,
                        "Test",
                        "Test",
                        passwordEncoder.encode(PASSWORD),
                        new Date()
                )
        );

        tokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        EMAIL,
                        PASSWORD
                )
        ));

        locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1",
                        "Описание"
                )
        ));

        firstServiceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 1",
                        null
                )
        ));
        secondServiceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 2",
                        null
                )
        ));

        confirmedClientModel = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                     null,
                        List.of(firstServiceModel.getId(), secondServiceModel.getId()),
                        null,
                        false
                )
        ));

        nonConfirmedClientModel = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        "+79193058732",
                        List.of(firstServiceModel.getId(), secondServiceModel.getId()),
                        null,
                        true
                )
        ));

        SpecialistModel firstSpecialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 1",
                        null,
                        List.of(firstServiceModel.getId())
                )
        ));
        SpecialistModel secondSpecialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 2",
                        null,
                        List.of(secondServiceModel.getId())
                )
        ));

        firstQueueModel = assertDoesNotThrow(() -> queueService.createQueue(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        firstSpecialistModel.getId(),
                        "Queue 1",
                        null
                )
        ));
        secondQueueModel = assertDoesNotThrow(() -> queueService.createQueue(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        secondSpecialistModel.getId(),
                        "Queue 2",
                        null
                )
        ));
    }

    @Test
    void testChangeClientValidation() {
        assertThrows(AccountIsNotAuthorizedException.class, () -> clientService.changeClient(
                localizer,
                "Invalid access token",
                locationModel.getId(),
                null,
                new ChangeClientRequest()
        ));
        assertThrows(DescriptionException.class, () -> clientService.changeClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                null,
                new ChangeClientRequest(
                       new HashMap<>()
                )
        ));
        assertThrows(DescriptionException.class, () -> clientService.changeClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                NON_EXISTING_ID,
                new ChangeClientRequest(
                        new HashMap<>()
                )
        ));
    }

    @Test
    void testCallAndReturnClient() {
        assertThrows(DescriptionException.class, () -> clientService.callClient(
                localizer,
                tokens.getAccess(),
                NON_EXISTING_ID,
                confirmedClientModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> clientService.callClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                NON_EXISTING_ID
        ));
        assertDoesNotThrow(() -> clientService.callClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                confirmedClientModel.getId()
        ));

        Optional<QueueEntity> queueAfterCall = queueRepo.findById(firstQueueModel.getId());
        assertTrue(queueAfterCall.isPresent());
        QueueEntity queueEntityAfterCall = queueAfterCall.get();
        assertEquals(confirmedClientModel.getId(), queueEntityAfterCall.getClientId());

        assertThrows(DescriptionException.class, () -> clientService.returnClient(
                localizer,
                tokens.getAccess(),
                NON_EXISTING_ID,
                confirmedClientModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> clientService.returnClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                NON_EXISTING_ID
        ));
        assertDoesNotThrow(() -> clientService.returnClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                confirmedClientModel.getId()
        ));

        Optional<QueueEntity> queueAfterReturn = queueRepo.findById(firstQueueModel.getId());
        assertTrue(queueAfterReturn.isPresent());
        QueueEntity queueEntityAfterReturn = queueAfterReturn.get();
        assertNull(queueEntityAfterReturn.getClientId());
    }

    @Test
    void testConfirmAccessKeyByClient() {
        Optional<ClientEntity> confirmedClient = clientRepo.findById(confirmedClientModel.getId());
        assertTrue(confirmedClient.isPresent());

        assertThrows(DescriptionException.class, () -> clientService.confirmAccessKeyByClient(
                localizer,
                confirmedClientModel.getId(),
                confirmedClient.get().getAccessKey()
        ));

        Optional<ClientEntity> nonConfirmedClientBefore = clientRepo.findById(nonConfirmedClientModel.getId());
        assertTrue(nonConfirmedClientBefore.isPresent());
        assertEquals(ClientStatusEntity.Status.RESERVED.name(), nonConfirmedClientBefore.get().getStatus());

        assertDoesNotThrow(() -> clientService.confirmAccessKeyByClient(
                localizer,
                nonConfirmedClientModel.getId(),
                nonConfirmedClientBefore.get().getAccessKey()
        ));

        Optional<ClientEntity> nonConfirmedClientAfter = clientRepo.findById(nonConfirmedClientModel.getId());
        assertTrue(nonConfirmedClientAfter.isPresent());
        assertEquals(ClientStatusEntity.Status.CONFIRMED.name(), nonConfirmedClientAfter.get().getStatus());
    }

    @Test
    void testServeClient() {
        assertDoesNotThrow(() -> clientService.callClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                confirmedClientModel.getId()
        ));

        List<ClientToChosenServiceEntity> beforeFirstServing = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(confirmedClientModel.getId());
        List<Long> serviceIdsBeforeFirstServing = beforeFirstServing
                .stream()
                .map(item -> item.getPrimaryKey().getServiceId())
                .toList();
        assertEquals(2, serviceIdsBeforeFirstServing.size());
        assertTrue(serviceIdsBeforeFirstServing.contains(firstServiceModel.getId()));
        assertTrue(serviceIdsBeforeFirstServing.contains(secondServiceModel.getId()));

        assertDoesNotThrow(() -> clientService.serveClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                confirmedClientModel.getId(),
                new ServeClientRequest(
                        List.of(firstServiceModel.getId())
                )
        ));

        List<ClientToChosenServiceEntity> beforeSecondServing = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(confirmedClientModel.getId());
        List<Long> serviceIdsBeforeSecondServing = beforeSecondServing
                .stream()
                .map(item -> item.getPrimaryKey().getServiceId())
                .toList();
        assertEquals(1, serviceIdsBeforeSecondServing.size());
        assertTrue(serviceIdsBeforeSecondServing.contains(secondServiceModel.getId()));

        assertDoesNotThrow(() -> clientService.callClient(
                localizer,
                tokens.getAccess(),
                secondQueueModel.getId(),
                confirmedClientModel.getId()
        ));

        assertTrue(clientRepo.findById(confirmedClientModel.getId()).isPresent());

        assertDoesNotThrow(() -> clientService.serveClient(
                localizer,
                tokens.getAccess(),
                secondQueueModel.getId(),
                confirmedClientModel.getId(),
                new ServeClientRequest(
                        List.of(secondServiceModel.getId())
                )
        ));

        assertTrue(clientToChosenServiceRepo.findAllByPrimaryKeyClientId(confirmedClientModel.getId()).isEmpty());
        assertTrue(clientRepo.findById(confirmedClientModel.getId()).isEmpty());
    }

    @Test
    void testNotifyClient() {
        assertThrows(DescriptionException.class, () -> clientService.notifyClient(
                localizer,
                tokens.getAccess(),
                NON_EXISTING_ID,
                nonConfirmedClientModel.getId()
        ));
        assertThrows(DescriptionException.class, () -> clientService.notifyClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                NON_EXISTING_ID
        ));
        assertThrows(DescriptionException.class, () -> clientService.notifyClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                confirmedClientModel.getId()
        ));
        assertDoesNotThrow(() -> clientService.notifyClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                nonConfirmedClientModel.getId()
        ));
    }

    @Test
    void testCreateClientWithSamePhone() {
        assertThrows(DescriptionException.class, () -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        nonConfirmedClientModel.getPhone(),
                        List.of(firstServiceModel.getId(), secondServiceModel.getId()),
                        null,
                        true
                )
        ));
    }

    @Test
    void testDeleteClientWhenClientInQueue() {
        assertDoesNotThrow(() -> clientService.callClient(
                localizer,
                tokens.getAccess(),
                firstQueueModel.getId(),
                confirmedClientModel.getId()
        ));

        assertTrue(clientRepo.findById(confirmedClientModel.getId()).isPresent());

        assertDoesNotThrow(() -> clientService.deleteClientInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                confirmedClientModel.getId()
        ));

        assertTrue(clientRepo.findById(confirmedClientModel.getId()).isEmpty());
    }

    @Test
    void testCustomQueryWithJoinServicesForClient() {
        ServiceModel thirdServiceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 3",
                        null
                )
        ));

        ClientModel localClientModel = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        null,
                        List.of(firstServiceModel.getId(), secondServiceModel.getId()),
                        null,
                        false
                )
        ));

        assertTrue(serviceRepo.findById(firstServiceModel.getId()).isPresent());
        assertTrue(serviceRepo.findById(secondServiceModel.getId()).isPresent());
        assertTrue(serviceRepo.findById(thirdServiceModel.getId()).isPresent());

        List<ServiceModel> servicesForClient = serviceRepo.findAllByLocationIdAndAssignedToClient(locationModel.getId(), localClientModel.getId())
                .stream()
                .map(ServiceModel::toModel)
                .toList();

        assertTrue(servicesForClient.contains(firstServiceModel));
        assertTrue(servicesForClient.contains(secondServiceModel));
        assertFalse(servicesForClient.contains(thirdServiceModel));
    }

    @Test
    void testCreateClientWithPreparedServicesSequence() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                        "Location 2",
                        "Description"
                )
        ));

        ServiceModel firstServiceOrder1 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 1",
                        "Description 1"
                )
        ));
        ServiceModel secondServiceOrder2 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 2",
                        "Description 2"
                )
        ));
        ServiceModel thirdServiceOrder2 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 3 (without description)",
                        null
                )
        ));
        ServiceModel fourthServiceOrder3 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 4 (without description)",
                        null
                )
        ));

        Map<Long, Integer> serviceIdsToOrderNumbers = Map.ofEntries(
                entry(firstServiceOrder1.getId(), 1),
                entry(secondServiceOrder2.getId(), 2),
                entry(thirdServiceOrder2.getId(), 2),
                entry(fourthServiceOrder3.getId(), 3)
        );

        ServicesSequenceModel servicesSequence = assertDoesNotThrow(() -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        "ServicesSequence",
                        "Description",
                        serviceIdsToOrderNumbers
                )
        ));

        ClientModel client = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        null,
                        null,
                        servicesSequence.getId(),
                        false
                )
        ));

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(client.getId());
        HashMap<Long, Integer> actual = new HashMap<>();
        for (ClientToChosenServiceEntity clientToChosenServiceEntity : clientToChosenServiceEntities) {
            actual.put(
                    clientToChosenServiceEntity.getPrimaryKey().getServiceId(),
                    clientToChosenServiceEntity.getOrderNumber()
            );
        }
        assertEquals(serviceIdsToOrderNumbers, actual);
    }

    @Test
    void testChangeClient() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                tokens.getAccess(),
                new CreateLocationRequest(
                        "Location 2",
                        "Description"
                )
        ));

        ServiceModel firstServiceOrder1 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 1",
                        "Description 1"
                )
        ));
        ServiceModel secondServiceOrder2 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 2",
                        "Description 2"
                )
        ));
        ServiceModel thirdServiceOrder2 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 3 (without description)",
                        null
                )
        ));
        ServiceModel fourthServiceOrder3 = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 4 (without description)",
                        null
                )
        ));

        ClientModel client = assertDoesNotThrow(() -> clientService.createClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateClientRequest(
                        null,
                        List.of(firstServiceOrder1.getId()),
                        null,
                        false
                )
        ));

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(client.getId());

        assertEquals(1, clientToChosenServiceEntities.size());
        assertEquals(firstServiceOrder1.getId(), clientToChosenServiceEntities.get(0).getPrimaryKey().getServiceId());
        assertEquals(1, clientToChosenServiceEntities.get(0).getOrderNumber());

        Map<Long, Integer> serviceIdsToOrderNumbers = Map.ofEntries(
                entry(firstServiceOrder1.getId(), 1),
                entry(secondServiceOrder2.getId(), 2),
                entry(thirdServiceOrder2.getId(), 2),
                entry(fourthServiceOrder3.getId(), 3)
        );

        assertDoesNotThrow(() -> clientService.changeClient(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                client.getId(),
                new ChangeClientRequest(
                        serviceIdsToOrderNumbers
                )
        ));

        clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(client.getId());
        HashMap<Long, Integer> actual = new HashMap<>();
        for (ClientToChosenServiceEntity clientToChosenServiceEntity : clientToChosenServiceEntities) {
            actual.put(
                    clientToChosenServiceEntity.getPrimaryKey().getServiceId(),
                    clientToChosenServiceEntity.getOrderNumber()
            );
        }
        assertEquals(serviceIdsToOrderNumbers, actual);
    }
}
