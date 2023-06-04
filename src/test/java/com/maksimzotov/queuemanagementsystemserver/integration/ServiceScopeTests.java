package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.extension.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.client.ClientModel;
import com.maksimzotov.queuemanagementsystemserver.model.client.CreateClientRequest;
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
public class ServiceScopeTests {

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

        accountEntity = accountRepo.save(
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
                        "Location 1",
                        "Description"
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
                        "Services sequence",
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
                NON_EXISTING_ID
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
                NON_EXISTING_ID
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
                NON_EXISTING_ID
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
