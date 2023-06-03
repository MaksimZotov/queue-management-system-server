package com.maksimzotov.queuemanagementsystemserver.integration.scenarios;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.util.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RightsRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.SneakyThrows;
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
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                        "Локация 1",
                        "Описание"
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
    void testCreateServiceByAccountWithoutRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1",
                        "Описание"
                )
        ));
        assertThrows(DescriptionException.class, () -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 1",
                        "Описание"
                )
        ));
    }

    @Test
    void testCreateServiceByAccountWithRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                       "Локация 1",
                        "Описание"
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
        assertDoesNotThrow(() -> serviceService.createServiceInLocation(
                localizer,
                secondTokens.getAccess(),
                locationModel.getId(),
                new CreateServiceRequest(
                        "Услуга 1",
                        "Описание"
                )
        ));
    }

    @Test
    void testAddRightsByAccountWithEmployeeRights() {
        LocationModel locationModel = assertDoesNotThrow(() -> locationService.createLocation(
                localizer,
                firstTokens.getAccess(),
                new CreateLocationRequest(
                        "Локация 1",
                        "Описание"
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
                        "Локация 1",
                        "Описание"
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
}
