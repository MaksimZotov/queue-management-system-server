package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.CreateClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ChangeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ServeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/clients")
@EqualsAndHashCode(callSuper = true)
public class ClientController extends BaseController {

    private final ClientService clientService;

    public ClientController(MessageSource messageSource, ClientService clientService) {
        super(messageSource);
        this.clientService = clientService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClient(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateClientRequest createClientRequest
    ) {
        try {
            return ResponseEntity.ok().body(clientService.createClient(getLocalizer(request), getToken(request), locationId, createClientRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{client_id}/confirm")
    public ResponseEntity<?> confirmAccessKeyByClient(
            HttpServletRequest request,
            @PathVariable("client_id") Long clientId,
            @RequestParam("access_key") Integer accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.confirmAccessKeyByClient(getLocalizer(request), clientId, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{client_id}/state")
    public ResponseEntity<?> getQueueStateForClient(
            HttpServletRequest request,
            @PathVariable("client_id") Long clientId,
            @RequestParam("access_key") Integer accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.getQueueStateForClient(getLocalizer(request), clientId, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{client_id}/delete")
    public ResponseEntity<?> deleteClientInLocation(
            HttpServletRequest request,
            @PathVariable(name = "client_id") Long clientId,
            @RequestParam(name = "location_id") Long locationId
    ) {
        try {
            clientService.deleteClientInLocation(getLocalizer(request), getToken(request), locationId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{client_id}/change")
    public ResponseEntity<?> changeClientInLocation(
            HttpServletRequest request,
            @PathVariable("client_id") Long clientId,
            @RequestParam("location_id") Long locationId,
            @RequestBody ChangeClientRequest changeClientRequest
    ) {
        try {
            clientService.changeClient(getLocalizer(request), getToken(request), locationId, clientId, changeClientRequest);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{client_id}/serve")
    public ResponseEntity<?> serveClientInQueue(
            HttpServletRequest request,
            @PathVariable("client_id") Long clientId,
            @RequestParam("queue_id") Long queueId,
            @RequestBody ServeClientRequest serveClientRequest
    ) {
        try {
            clientService.serveClient(getLocalizer(request), getToken(request), queueId, clientId, serveClientRequest);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{client_id}/call")
    public ResponseEntity<?> callClientToQueue(
            HttpServletRequest request,
            @PathVariable(name = "client_id") Long clientId,
            @RequestParam(name = "queue_id") Long queueId
    ) {
        try {
            clientService.callClient(getLocalizer(request), getToken(request), queueId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{client_id}/return")
    public ResponseEntity<?> returnClientToQueue(
            HttpServletRequest request,
            @PathVariable(name = "client_id") Long clientId,
            @RequestParam(name = "queue_id") Long queueId
    ) {
        try {
            clientService.returnClient(getLocalizer(request), getToken(request), queueId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{client_id}/notify")
    public ResponseEntity<?> notifyClientInQueue(
            HttpServletRequest request,
            @PathVariable("client_id") Long clientId,
            @RequestParam("queue_id") Long queueId
    ) {
        try {
            clientService.notifyClient(getLocalizer(request), getToken(request), queueId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
