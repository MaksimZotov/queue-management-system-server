package com.maksimzotov.queuemanagementsystemserver.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
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
        } catch (TokenExpiredException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Token expired"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{location_id}/delete")
    public ResponseEntity<?> deleteLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            currentAccountService.handleRequestFromCurrentAccountNoReturn(
                    request,
                    username -> locationService.deleteLocation(username, locationId)
            );
            return ResponseEntity.ok().build();
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Token expired"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
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
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Token expired"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
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
        } catch (AccountIsNotAuthorizedException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Account is not authorized"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(403).body(new ErrorResult("Token expired"));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
