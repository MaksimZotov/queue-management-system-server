package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.service.CurrentAccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
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
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteQueue(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        try {
            Long deletedId = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> queueService.deleteQueue(username, id)
            );
            if (deletedId != null) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(new ErrorResult("Deletion failed"));
            }
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Account is not authorized"));
        }
    }

    @GetMapping()
    public ResponseEntity<?> getQueues(
            HttpServletRequest request,
            @RequestParam(name = "location_id") Long locationId,
            @RequestParam Integer page,
            @RequestParam(name = "page_size") Integer pageSize
    ) {
        try {
            ContainerForList<Queue> container = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> queueService.getQueues(locationId, page, pageSize)
            );
            return ResponseEntity.ok().body(container);
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
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
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinQueue(
            @PathVariable Long id,
            @RequestBody JoinQueueRequest joinQueueRequest
    ) {
        try {
            return ResponseEntity.ok().body(queueService.joinQueue(id, joinQueueRequest));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Join failed"));
        }
    }

    @PostMapping("/{id}/clients/{client_id}/serve")
    public ResponseEntity<?> serveClientInQueue(
            HttpServletRequest request,
            @PathVariable Long id,
            @PathVariable("client_id") Long clientId
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> queueService.serveClientInQueue(username, id, clientId)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }
    }

    @PostMapping("/{id}/clients/{client_id}/notify")
    public ResponseEntity<?> notifyClientInQueue(
            HttpServletRequest request,
            @PathVariable Long id,
            @PathVariable("client_id") Long clientId
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> queueService.notifyClientInQueue(username, id, clientId)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }
    }
}
