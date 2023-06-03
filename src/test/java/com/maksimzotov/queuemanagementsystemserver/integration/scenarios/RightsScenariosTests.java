package com.maksimzotov.queuemanagementsystemserver.integration.scenarios;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.util.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RightsRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceRepo;
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
public class RightsScenariosTests {

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RightsService rightsService;
    @Autowired
    private SpecialistService specialistService;
    @Autowired ServicesSequenceService servicesSequenceService;
    @Autowired QueueService queueService;

    @Autowired
    private RightsRepo rightsRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private LocationRepo locationRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokensResponse firstTokens;
    private TokensResponse secondTokens;

    @BeforeEach
    void beforeEach() {
        rightsRepo.deleteAll();
        serviceRepo.deleteAll();
        locationRepo.deleteAll();
        accountRepo.deleteAll();

        accountRepo.save(
                new AccountEntity(
                        1L,
                        "zotovm256@gmail.com",
                        "Test",
                        "Test",
                        passwordEncoder.encode("12345678"),
                        new Date()
                )
        );
        accountRepo.save(
                new AccountEntity(
                        2L,
                        "zotovmaksim1254@gmail.com",
                        "Test",
                        "Test",
                        passwordEncoder.encode("12345678"),
                        new Date()
                )
        );

        firstTokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com" ,
                        "12345678"
                )
        ));
        secondTokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        "zotovmaksim1254@gmail.com" ,
                        "12345678"
                )
        ));

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );
    }

    @Test
    void testAddRightsByAccountWithoutRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    void testPerformOperationsByAccountWithoutRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertThrows(DescriptionException.class, () -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        null,
                        null
                )
        ));
        assertThrows(DescriptionException.class, () -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        null,
                        null,
                        null
                )
        ));
        assertThrows(DescriptionException.class, () -> specialistService.createSpecialistInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        null,
                        null,
                        null
                )
        ));
        assertThrows(DescriptionException.class, () -> queueService.createQueue(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        null,
                        null,
                        null
                )
        ));
    }

    @Test
    void testPerformOperationsByAccountWithRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                       "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
        ServiceModel serviceModel = assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Service 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> servicesSequenceService.createServicesSequenceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServicesSequenceRequest(
                        "Services Sequence 1",
                        "Description",
                        Map.ofEntries(
                                entry(serviceModel.getId(), 1)
                        )
                )
        ));
        SpecialistModel specialistModel = assertDoesNotThrow(() -> specialistService.createSpecialistInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateSpecialistRequest(
                        "Specialist 1",
                        "Description",
                        List.of(serviceModel.getId())
                )
        ));
        assertDoesNotThrow(() -> queueService.createQueue(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateQueueRequest(
                        specialistModel.getId(),
                        "Queue 1",
                        "Description"
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithEmployeeRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "test1234567tyiefse4@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithAdministratorRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.ADMINISTRATOR.name()
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "test1234567tyiefse4@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithRemovedAdministratorRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Location 1",
                        "Description"
                )
        ));
        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "zotovmaksim1254@gmail.com",
                        RightsStatusEntity.Status.ADMINISTRATOR.name()
                )
        ));

        assertDoesNotThrow(() -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "test1234567tyiefse4@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));

        List<String> rightsModelsBefore = assertDoesNotThrow(() -> rightsService.getRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId()
        )).getResults()
                .stream()
                .map(RightsModel::getEmail)
                .toList();
        assertTrue(rightsModelsBefore.contains("test1234567tyiefse4@gmail.com"));
        assertTrue(rightsModelsBefore.contains("zotovmaksim1254@gmail.com"));

        assertDoesNotThrow(() -> rightsService.deleteRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                "test1234567tyiefse4@gmail.com"
        ));
        assertDoesNotThrow(() -> rightsService.deleteRights(
                localizer,
                firstTokens.getAccess(),
                locationModel.getId(),
                "zotovmaksim1254@gmail.com"
        ));

        assertTrue(
                assertDoesNotThrow(() -> rightsService.getRights(
                        localizer,
                        firstTokens.getAccess(),
                        locationModel.getId()
                )).getResults().isEmpty()
        );

        assertThrows(DescriptionException.class, () -> rightsService.addRights(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new AddRightsRequest(
                        "test1234567tyiefse4@gmail.com",
                        RightsStatusEntity.Status.EMPLOYEE.name()
                )
        ));
    }
}
