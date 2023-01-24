package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.controller.base.BaseController;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/locations")
@EqualsAndHashCode(callSuper = true)
public class LocationController extends BaseController {

    private final LocationService locationService;

    public LocationController(MessageSource messageSource, LocationService locationService) {
        super(messageSource);
        this.locationService = locationService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLocation(
            HttpServletRequest request,
            @RequestBody CreateLocationRequest createLocationRequest
    ) {
        try {
            return ResponseEntity.ok().body(locationService.createLocation(getLocalizer(request), getToken(request), createLocationRequest));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @DeleteMapping("/{location_id}/delete")
    public ResponseEntity<?> deleteLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            locationService.deleteLocation(getLocalizer(request), getToken(request), locationId);
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}")
    public ResponseEntity<?> getLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocation(getLocalizer(request), getToken(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping()
    public ResponseEntity<?> getLocations(
            HttpServletRequest request,
            @RequestParam String username
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocations(getLocalizer(request), getToken(request), username));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkHasRights(
            HttpServletRequest request,
            @RequestParam String username
    ) {
        return ResponseEntity.ok().body(locationService.checkHasRights(getToken(request), username));
    }

    @PostMapping("/{location_id}/change")
    public ResponseEntity<?> changeMaxColumns(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @RequestParam("max_columns") Integer maxColumns
    ) {
        try {
            return ResponseEntity.ok().body(locationService.changeMaxColumns(getLocalizer(request), getToken(request), locationId, maxColumns));
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(401).body(new ErrorResult(getLocalizer(request).getMessage(Message.ACCOUNT_IS_NOT_AUTHORIZED)));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }
}
