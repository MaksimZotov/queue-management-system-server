package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.service.SpecialistService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/specialists")
@EqualsAndHashCode(callSuper = true)
public class SpecialistController extends BaseController {

    private final SpecialistService specialistService;

    public SpecialistController(MessageSource messageSource, SpecialistService specialistService) {
        super(messageSource);
        this.specialistService = specialistService;
    }

    @GetMapping
    public ResponseEntity<?> getSpecialistsInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(specialistService.getSpecialistsInLocation(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSpecialistInLocation(
            HttpServletRequest request,
            @RequestParam("location_id") Long locationId,
            @RequestBody CreateSpecialistRequest createSpecialistRequest
    ) {
        try {
            return ResponseEntity.ok().body(specialistService.createSpecialistInLocation(getLocalizer(request), getToken(request), locationId, createSpecialistRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{specialist_id}/delete")
    public ResponseEntity<?> deleteSpecialistInLocation(
            HttpServletRequest request,
            @PathVariable("specialist_id") Long specialistId,
            @RequestParam("location_id") Long locationId
    ) {
        try {
            specialistService.deleteSpecialistInLocation(getLocalizer(request), getToken(request), locationId, specialistId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        }  catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
