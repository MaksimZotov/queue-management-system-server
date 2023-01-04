package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/{queue_id}/client")
    public ResponseEntity<?> getQueueStateForClient(
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.getQueueStateForClient(queueId, email, accessKey));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queue_id}/client/join")
    public ResponseEntity<?> joinQueue(
            @PathVariable("queue_id") Long queueId,
            @RequestBody JoinQueueRequest joinQueueRequest
    ) {
        try {
            return ResponseEntity.ok().body(clientService.joinQueue(queueId, joinQueueRequest));
        } catch (FieldsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getErrors()));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queue_id}/client/rejoin")
    public ResponseEntity<?> rejoinQueue(
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email
    ) {
        try {
            return ResponseEntity.ok().body(clientService.rejoinQueue(queueId, email));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queue_id}/client/confirm")
    public ResponseEntity<?> confirmCode(
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email,
            @RequestParam String code
    ) {
        try {
            return ResponseEntity.ok().body(clientService.confirmCode(queueId, email, code));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{queue_id}/client/leave")
    public ResponseEntity<?> leaveQueue(
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email,
            @RequestParam("access_key") String accessKey
    ) {
        try {
            return ResponseEntity.ok().body(clientService.leaveQueue(queueId, email, accessKey));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
