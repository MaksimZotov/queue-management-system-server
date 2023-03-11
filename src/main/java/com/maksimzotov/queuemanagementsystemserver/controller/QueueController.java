package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.client.ServeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
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
    private final ServiceService serviceService;
    private final ClientService clientService;

    public QueueController(
            MessageSource messageSource,
            QueueService queueService,
            ServiceService serviceService,
            ClientService clientService
    ) {
        super(messageSource);
        this.queueService = queueService;
        this.serviceService = serviceService;
        this.clientService = clientService;
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

    @GetMapping("/{queue_id}")
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

    @PostMapping("/{queue_id}/enable")
    public ResponseEntity<?> enable(
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

    @PostMapping("/{queue_id}/disable")
    public ResponseEntity<?> disable(
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

    @PostMapping("/serve")
    public ResponseEntity<?> serveClientInQueue(
            HttpServletRequest request,
            @RequestBody ServeClientRequest serveClientRequest
    ) {
        try {
            clientService.serveClientInQueueByEmployee(getLocalizer(request), getToken(request), serveClientRequest);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/{queue_id}/call")
    public ResponseEntity<?> callClientToQueue(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId,
            @RequestParam(name = "client_id") Long clientId
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

    @PostMapping("/{queue_id}/return")
    public ResponseEntity<?> returnClient(
            HttpServletRequest request,
            @PathVariable(name = "queue_id") Long queueId,
            @RequestParam(name = "client_id") Long clientId
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

    @PostMapping("/{queue_id}/notify")
    public ResponseEntity<?> notifyClientInQueue(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId,
            @RequestParam("client_id") Long clientId
    ) {
        try {
            clientService.notifyClientInQueueByEmployee(getLocalizer(request), getToken(request), queueId, clientId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{queue_id}/service")
    public ResponseEntity<?> getServicesInQueue(
            HttpServletRequest request,
            @PathVariable("queue_id") Long queueId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesInQueue(getLocalizer(request), queueId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/specialists/{specialist_id}")
    public ResponseEntity<?> getServicesSpecialist(
            HttpServletRequest request,
            @PathVariable("specialist_id") Long specialistId
    ) {
        try {
            return ResponseEntity.ok().body(serviceService.getServicesInSpecialist(getLocalizer(request), specialistId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
