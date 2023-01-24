package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.queue.AddClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/queues")
@EqualsAndHashCode(callSuper = true)
public class QueueController extends BaseController {

    private final QueueService queueService;

    public QueueController(MessageSource messageSource, QueueService queueService) {
        super(messageSource);
        this.queueService = queueService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQueue(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateQueueRequest createQueueRequest
    ) {
        try {
            return ResponseEntity.ok().body(queueService.createQueue(getLocalizer(request), getToken(request), locationId, createQueueRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{queue_id}/delete")
    public ResponseEntity<?> deleteQueue(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId
    ) {
        try {
            queueService.deleteQueue(getLocalizer(request), getToken(request), queueId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping()
    public ResponseEntity<?> getQueues(
            HttpServletRequest request,
            @RequestParam(name = "location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(queueService.getQueues(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{queueId}")
    public ResponseEntity<?> getQueueState(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId
    ) {
        try {
            return ResponseEntity.ok().body(queueService.getQueueState(getLocalizer(request), getToken(request), queueId));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/serve")
    public ResponseEntity<?> serveClientInQueue(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId,
            @RequestParam(name = "client_id") Long clientId
    ) {
        try {
            queueService.serveClientInQueue(getLocalizer(request), getToken(request), queueId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/notify")
    public ResponseEntity<?> notifyClientInQueue(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId,
            @RequestParam(name = "client_id") Long clientId
    ) {
        try {
            queueService.notifyClientInQueue(getLocalizer(request), getToken(request), queueId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/client/add")
    public ResponseEntity<?> addClient(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestBody AddClientRequest addClientRequest
    ) {
        try {
            return ResponseEntity.ok().body(queueService.addClient(getLocalizer(request), getToken(request), queueId, addClientRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/pause")
    public ResponseEntity<?> pause(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId
    ) {
        try {
            queueService.changePausedState(getLocalizer(request), getToken(request), queueId, true);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/start")
    public ResponseEntity<?> start(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId
    ) {
        try {
            queueService.changePausedState(getLocalizer(request), getToken(request), queueId, false);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/pause")
    public ResponseEntity<?> pauseAll(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            queueService.changePausedStateInLocation(getLocalizer(request), getToken(request), locationId, true);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startAll(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            queueService.changePausedStateInLocation(getLocalizer(request), getToken(request), locationId, false);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{id}/switch")
    public ResponseEntity<?> switchClientLateState(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestParam(name = "client_id") Long clientId,
            @RequestParam Boolean late
    ) {
        try {
            queueService.switchClientLateState(getLocalizer(request), getToken(request), queueId, clientId, late);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
