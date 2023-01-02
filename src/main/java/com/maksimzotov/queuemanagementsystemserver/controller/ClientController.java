package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/{username}/locations/{location_id}/queues/{queue_id}/join")
    public ResponseEntity<?> joinQueue(
            @PathVariable String username,
            @PathVariable("location_id") String locationId,
            @PathVariable("queue_id") Long queueId,
            @RequestBody JoinQueueRequest joinQueueRequest
    ) {
        try {
            return ResponseEntity.ok().body(clientService.joinQueue(queueId, joinQueueRequest));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Join failed"));
        }
    }

    @GetMapping("/{username}/locations/{location_id}/queues/{queue_id}/client")
    public ResponseEntity<?> getClientInQueueState(
            @PathVariable String username,
            @PathVariable("location_id") String locationId,
            @PathVariable("queue_id") Long queueId,
            @RequestParam String email
    ) {
        try {
            return ResponseEntity.ok().body(clientService.getClientInQueueState(queueId, email));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Join failed"));
        }
    }
}
