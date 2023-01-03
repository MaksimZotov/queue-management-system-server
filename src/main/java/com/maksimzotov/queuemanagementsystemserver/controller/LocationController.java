package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.service.CurrentAccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final CurrentAccountService currentAccountService;
    private final LocationService locationService;

    @PostMapping("/create")
    public ResponseEntity<?> createLocation(
            HttpServletRequest request,
            @RequestBody CreateLocationRequest createLocationRequest
    ) {
        try {
            Location location = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> locationService.createLocation(username, createLocationRequest)
            );
            return ResponseEntity.ok().body(location);
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Fail"));
        }
    }

    @DeleteMapping("/{location_id}/delete")
    public ResponseEntity<?> deleteLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            Long deletedId = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> locationService.deleteLocation(username, locationId)
            );
            if (deletedId != null) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body(new ErrorResult("Deletion failed"));
            }
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Account is not authorized"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Fail"));
        }
    }

    @GetMapping("/{location_id}")
    public ResponseEntity<?> getLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId,
            @RequestParam("username") String username
    ) {
        try {
            if (Objects.equals(username, "me")) {
                return ResponseEntity.ok().body(
                        currentAccountService.handleRequestFromCurrentAccount(
                                request,
                                profileUsername -> locationService.getLocation(locationId, true)
                        )
                );
            }
            return ResponseEntity.ok().body(locationService.getLocation(locationId, false));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Fail"));
        }
    }

    @GetMapping()
    public ResponseEntity<?> getLocations(
            HttpServletRequest request,
            @RequestParam String username,
            @RequestParam Integer page,
            @RequestParam(name = "page_size") Integer pageSize
    ) {
        log.info("getMyLocations() called");
        try {
            ContainerForList<Location> container;
            if (Objects.equals(username, "me")) {
                container = currentAccountService.handleRequestFromCurrentAccount(
                        request,
                        profileUsername -> locationService.getLocations(profileUsername, page, pageSize, true)
                );
            } else {
                container = locationService.getLocations(username, page, pageSize, false);
            }
            return ResponseEntity.ok().body(container);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Fail"));
        }
    }
}
