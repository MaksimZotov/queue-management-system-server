package com.maksimzotov.queuemanagementsystemserver.model.rights;

import lombok.Value;

@Value
public class AddRightsRequest {
    String email;
    String status;
}
