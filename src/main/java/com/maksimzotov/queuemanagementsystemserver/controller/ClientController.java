package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
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
            HttpServletRequest request,
            @RequestParam("client_id") Long clientId,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.getQueueStateForClient(getLocalizer(request), clientId, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAccessKeyByClient(
            HttpServletRequest request,
            @RequestParam("client_id") Long clientId,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.confirmAccessKeyByClient(getLocalizer(request), clientId, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveByClient(
            HttpServletRequest request,
            @RequestParam("client_id") Long clientId,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.leaveByClient(getLocalizer(request), clientId, accessKey));
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
}
