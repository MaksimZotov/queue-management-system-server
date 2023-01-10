package com.maksimzotov.queuemanagementsystemserver.controller;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.AddClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.service.CurrentAccountService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
@Slf4j
public class QueueController {

    private final CurrentAccountService currentAccountService;
    private final QueueService queueService;

    @PostMapping("/create")
    public ResponseEntity<?> createQueue(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateQueueRequest createQueueRequest
    ) {
        try {
            Queue queue = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> queueService.createQueue(username, locationId, createQueueRequest)
            );
            return ResponseEntity.ok().body(queue);
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteQueue(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> queueService.deleteQueue(username, id)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Аккаунт не авторизован"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping()
    public ResponseEntity<?> getQueues(
            HttpServletRequest request,
            @RequestParam String username,
            @RequestParam(name = "location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(
                    currentAccountService.handleRequestFromCurrentAccount(
                            request,
                            profileUsername -> queueService.getQueues(locationId, true)
                    )
            );
        } catch (AccountIsNotAuthorizedException | TokenExpiredException | JWTDecodeException ex) {
            try {
                return ResponseEntity.ok().body(queueService.getQueues(locationId, false));
            } catch (DescriptionException nestedException) {
                return ResponseEntity.badRequest().body(new ErrorResult(nestedException.getDescription()));
            } catch (Exception nestedException) {
                return ResponseEntity.internalServerError().build();
            }
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQueueState(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        try {
            QueueState state = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> queueService.getQueueState(id)
            );
            return ResponseEntity.ok().body(state);
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Аккаунт не авторизован"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/serve")
    public ResponseEntity<?> serveClientInQueue(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(name = "client_id") Long clientId
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> queueService.serveClientInQueue(username, id, clientId)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Аккаунт не авторизован"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/notify")
    public ResponseEntity<?> notifyClientInQueue(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(name = "client_id") Long clientId
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> queueService.notifyClientInQueue(username, id, clientId)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queue_id}/client/add")
    public ResponseEntity<?> addClient(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestBody AddClientRequest addClientRequest
    ) {
        try {
            return ResponseEntity.ok().body(
                    currentAccountService.handleRequestFromCurrentAccount(
                            request,
                            username -> queueService.addClient(queueId, addClientRequest)
                    )
            );
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(401).body(new ErrorResult("Время действия токена истекло"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
