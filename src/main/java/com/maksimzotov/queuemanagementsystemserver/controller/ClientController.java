package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/client")
@EqualsAndHashCode(callSuper = true)
public class ClientController extends BaseController {

    private final ClientService clientService;

    public ClientController(MessageSource messageSource, ClientService clientService) {
        super(messageSource);
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<?> getQueueStateForClient(
            @RequestParam("email") String email,
            @RequestParam("access_key") String accessKey
    ) {
        return ResponseEntity.ok().body(clientService.getQueueStateForClient(email, accessKey));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinByClient(
            HttpServletRequest request,
            @RequestBody AddClientRequst addClientRequst,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(clientService.joinByClient(getLocalizer(request), locationId, addClientRequst));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/rejoin")
    public ResponseEntity<?> rejoinByClient(
            HttpServletRequest request,
            @RequestParam String email
    ) {
        try {
            return ResponseEntity.ok().body(clientService.rejoinByClient(getLocalizer(request), email));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmCodeByClient(
            HttpServletRequest request,
            @RequestParam String email,
            @RequestParam String code
    ) {
        try {
            return ResponseEntity.ok().body(clientService.confirmCodeByClient(getLocalizer(request), email, code));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveByClient(
            HttpServletRequest request,
            @RequestParam String email,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.leaveByClient(getLocalizer(request), email, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addClientByEmployee(
            HttpServletRequest request,
            @RequestBody AddClientRequst addClientRequst,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            clientService.addClientByEmployee(getLocalizer(request), getToken(request), locationId, addClientRequst);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
