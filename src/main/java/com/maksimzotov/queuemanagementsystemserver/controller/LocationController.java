package com.maksimzotov.queuemanagementsystemserver.controller;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.HasRightsInfo;
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
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        } catch (TokenExpiredException ex) {
            return ResponseEntity.status(401).body(new ErrorResult("Время действия токена истекло"));
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
            return ResponseEntity.badRequest().body(new ErrorResult("Аккаунт не авторизован"));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.status(401).body(new ErrorResult("Время действия токена истекло"));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{location_id}")
    public ResponseEntity<?> getLocation(
            HttpServletRequest request,
            @PathVariable("location_id") Long locationId
    ) {
        try {
            return ResponseEntity.ok().body(
                    currentAccountService.handleRequestFromCurrentAccount(
                            request,
                            profileUsername -> locationService.getLocation(locationId, profileUsername)
                    )
            );
        } catch (AccountIsNotAuthorizedException | TokenExpiredException | JWTDecodeException ex) {
            try {
                return ResponseEntity.ok().body(locationService.getLocation(locationId, null));
            }  catch (DescriptionException nestedException) {
                return ResponseEntity.badRequest().body(new ErrorResult(nestedException.getDescription()));
            } catch (Exception nestedException) {
                return ResponseEntity.internalServerError().build();
            }
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping()
    public ResponseEntity<?> getLocations(
            HttpServletRequest request,
            @RequestParam String username
    ) {
        try {
            return ResponseEntity.ok().body(
                    currentAccountService.handleRequestFromCurrentAccount(
                            request,
                            profileUsername -> locationService.getLocations(
                                    username,
                                    Objects.equals(profileUsername, username)
                            )
                    )
            );
        } catch (AccountIsNotAuthorizedException | TokenExpiredException | JWTDecodeException ex) {
            try {
                return ResponseEntity.ok().body(locationService.getLocations(username, false));
            } catch (Exception nestedException) {
                return ResponseEntity.internalServerError().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkHasRights(
            HttpServletRequest request,
            @RequestParam String username
    ) {
        try {
            boolean hasRights = currentAccountService.handleRequestFromCurrentAccount(
                    request,
                    profileUsername -> Objects.equals(profileUsername, username)
            );
            return ResponseEntity.ok().body(new HasRightsInfo(hasRights));
        } catch (AccountIsNotAuthorizedException | TokenExpiredException | JWTDecodeException ex) {
            return ResponseEntity.ok().body(new HasRightsInfo(false));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
