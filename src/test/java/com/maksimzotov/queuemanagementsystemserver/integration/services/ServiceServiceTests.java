package com.maksimzotov.queuemanagementsystemserver.integration.services;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.util.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.client.ClientModel;
import com.maksimzotov.queuemanagementsystemserver.model.client.CreateClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationState;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueModel;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.service.impl.RightsServiceImpl;
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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(PostgreSQLExtension.class)
@TestPropertySource(
        properties = {"spring.config.location=classpath:application-tests.yml"}
)
@DirtiesContext
public class ServiceServiceTests {
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
    private ServicesSequenceModel servicesSequenceModel;
    private SpecialistModel firstSpecialistModel;
    private SpecialistModel secondSpecialistModel;
    private QueueModel firstQueueModel;
    private QueueModel secondQueueModel;
    private ClientModel confirmedClientModel;
    private ClientModel nonConfirmedClientModel;
    private AccountEntity accountEntity;

    @BeforeEach
    void beforeEach() {
        queueRepo.deleteAll();
        specialistRepo.deleteAll();
        serviceRepo.deleteAll();
        serviceInServicesSequenceRepo.deleteAll();
        servicesSequenceRepo.deleteAll();
        locationRepo.deleteAll();
        accountRepo.deleteAll();
        clientToChosenServiceRepo.deleteAll();
        clientRepo.deleteAll();

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );

        accountEntity = accountRepo.save(
                new AccountEntity(
                        1L,
                        "zotovm256@gmail.com",
                        "Test",
                        "Test",
                        passwordEncoder.encode("12345678"),
                        new Date()
                )
        );

        tokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com",
                        "12345678"
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

        servicesSequenceModel = assertDoesNotThrow(() -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        "ServicesSequence",
                        "Description",
                        Map.ofEntries(
                                entry(firstServiceModel.getId(), 1),
                                entry(secondServiceModel.getId(), 1)
                        )
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

        firstSpecialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                tokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 1",
                        null,
                        List.of(firstServiceModel.getId())
                )
        ));
        secondSpecialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
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
    void testGetServicesInQueue() {
        assertThrows(DescriptionException.class, () -> serviceService.getServicesInQueue(
                localizer,
                1000L
        ));
        List<ServiceModel> serviceModels = assertDoesNotThrow(() -> serviceService.getServicesInQueue(
                localizer,
                firstQueueModel.getId()
        )).getResults();
        assertEquals(List.of(firstServiceModel), serviceModels);
    }

    @Test
    void testGetServicesInSpecialist() {
        assertThrows(DescriptionException.class, () -> serviceService.getServicesInSpecialist(
                localizer,
                1000L
        ));
        List<ServiceModel> serviceModels = assertDoesNotThrow(() -> serviceService.getServicesInQueue(
                localizer,
                firstSpecialistModel.getId()
        )).getResults();
        assertEquals(List.of(firstServiceModel), serviceModels);
    }

    @Test
    void testGetServicesInServicesSequence() {
        assertThrows(DescriptionException.class, () -> serviceService.getServicesInServicesSequence(
                localizer,
                1000L
        ));
        Map<Long, Integer> serviceIdsToOrderNumbers = assertDoesNotThrow(() -> serviceService.getServicesInServicesSequence(
                localizer,
                servicesSequenceModel.getId()
        )).getServiceIdsToOrderNumbers();
        assertEquals(
                Map.ofEntries(
                        entry(firstServiceModel.getId(), 1),
                        entry(secondServiceModel.getId(), 1)
                ),
                serviceIdsToOrderNumbers
        );
    }
}
