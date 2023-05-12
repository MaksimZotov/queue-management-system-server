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

    @GetMapping
    public ResponseEntity<?> getLocations(
            HttpServletRequest request,
            @RequestParam("account_id") Long accountId
    ) {
        try {
            return ResponseEntity.ok().body(locationService.getLocations(getLocalizer(request), accountId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
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
            return ResponseEntity.ok().body(locationService.getLocation(getLocalizer(request), locationId));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @GetMapping("/{location_id}/state")
    public ResponseEntity<?> getLocationState(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        return ResponseEntity.ok().body(locationService.getLocationState(getLocalizer(request), locationId));
    }
}
