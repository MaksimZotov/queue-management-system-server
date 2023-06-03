package com.maksimzotov.queuemanagementsystemserver.integration.scenarios;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.integration.util.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(PostgreSQLExtension.class)
@TestPropertySource(
        properties = {"spring.config.location=classpath:application-tests.yml"}
)
@DirtiesContext
public class ClientServicesScenariosTests {

    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private LocationRepo locationRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private ClientToChosenServiceRepo clientToChosenServiceRepo;

    private LocationEntity locationEntity;

    @BeforeEach
    void beforeEach() {
        accountRepo.deleteAll();
        clientRepo.deleteAll();
        locationRepo.deleteAll();
        serviceRepo.deleteAll();
        clientToChosenServiceRepo.deleteAll();

        AccountEntity accountEntity = accountRepo.save(
                new AccountEntity(
                        null,
                        "dfref5vgvef4vevdf4@gmail.com",
                        "Test",
                        "Test",
                        "12345678",
                        new Date()
                )
        );
        locationEntity = locationRepo.save(
                new LocationEntity(
                        null,
                        accountEntity.getEmail(),
                        "",
                        null
                )
        );
    }

    @Test
    void testCustomQueryWithJoin() {
        ServiceEntity firstServiceEntity = serviceRepo.save(
                new ServiceEntity(
                      null,
                      locationEntity.getId(),
                        "Тест 1",
                        null
                )
        );
        ServiceEntity secondServiceEntity = serviceRepo.save(
                new ServiceEntity(
                        null,
                        locationEntity.getId(),
                        "Тест 2",
                        null
                )
        );
        serviceRepo.save(
                new ServiceEntity(
                        null,
                        locationEntity.getId(),
                        "Тест 3",
                        null
                )
        );

        ClientEntity clientEntity = clientRepo.save(
                new ClientEntity(
                        null,
                        locationEntity.getId(),
                        "+71234567890",
                        1,
                        1234,
                        ClientStatusEntity.Status.CONFIRMED.name(),
                        new Date(),
                        new Date()
                )
        );

        clientToChosenServiceRepo.save(
                new ClientToChosenServiceEntity(
                        new ClientToChosenServiceEntity.PrimaryKey(
                                clientEntity.getId(),
                                firstServiceEntity.getId()
                        ),
                        1
                )
        );
        clientToChosenServiceRepo.save(
                new ClientToChosenServiceEntity(
                        new ClientToChosenServiceEntity.PrimaryKey(
                                clientEntity.getId(),
                                secondServiceEntity.getId()
                        ),
                        2
                )
        );

        List<Long> expected = Stream.of(firstServiceEntity, secondServiceEntity)
                .map(ServiceEntity::getId)
                .toList();

        List<Long> actual = serviceRepo.findAllByLocationIdAndAssignedToClient(locationEntity.getId(), clientEntity.getId()).stream()
                .map(ServiceEntity::getId)
                .toList();

        assertEquals(expected, actual);
    }
}
