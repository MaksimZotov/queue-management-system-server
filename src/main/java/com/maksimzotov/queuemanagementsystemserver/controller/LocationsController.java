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

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationsController {

    private final CurrentAccountService currentAccountService;
    private final LocationService locationService;

    @PostMapping()
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
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        try {
            Long deletedId = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> locationService.deleteLocation(username, id)
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

    @GetMapping("/me")
    public ResponseEntity<?> getMyLocations(
            HttpServletRequest request,
            @RequestParam Integer page,
            @RequestParam(name = "page_size") Integer pageSize
    ) {
        try {
            ContainerForList<Location> container = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    username -> locationService.getLocations(username, page, pageSize)
            );
            return ResponseEntity.ok().body(container);
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }
    }
}
