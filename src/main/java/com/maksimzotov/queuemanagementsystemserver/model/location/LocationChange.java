package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public abstract class LocationChange {

    Event event;

    public enum Event {
        ADD_CLIENT,
        UPDATE_CLIENT,
        DELETE_CLIENT
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class AddClient extends LocationChange {

        @Override
        public Event getEvent() {
            return Event.ADD_CLIENT;
        }

        LocationState.Client client;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateClient extends LocationChange {

        @Override
        public Event getEvent() {
            return Event.UPDATE_CLIENT;
        }

        LocationState.Client client;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DeleteClient extends LocationChange {

        @Override
        public Event getEvent() {
            return Event.DELETE_CLIENT;
        }

        @JsonProperty("client_id")
        Long clientId;
    }
}
