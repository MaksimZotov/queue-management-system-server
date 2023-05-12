package com.maksimzotov.queuemanagementsystemserver.message;

public enum Message {
    PASSWORD_MUST_CONTAINS_MORE_THAN_8_SYMBOLS,
    PASSWORD_MUST_CONTAINS_LESS_THAN_64_SYMBOLS,
    PASSWORDS_DO_NOT_MATCH,
    FIRST_NAME_MUST_NOT_BE_EMPTY,
    FIRST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS,
    LAST_NAME_MUST_NOT_BE_EMPTY,
    LAST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS,
    WRONG_EMAIL,
    WRONG_PHONE,
    WRONG_CODE,
    USER_WITH_EMAIL_RESERVED_START,
    USER_WITH_EMAIL_RESERVED_END,
    USER_WITH_EMAIL_ALREADY_EXISTS_START,
    USER_WITH_EMAIL_ALREADY_EXISTS_END,
    CONFIRMATION_OF_REGISTRATION,
    CODE_FOR_CONFIRMATION_OF_REGISTRATION,
    CODE_MUST_CONTAINS_4_SYMBOLS,
    CODE_EXPIRED,
    USER_WITH_EMAIL_DOES_NOT_EXIST_START,
    USER_WITH_EMAIL_DOES_NOT_EXIST_END,
    USER_WITH_EMAIL_IS_NOT_CONFIRMED_START,
    USER_WITH_EMAIL_IS_NOT_CONFIRMED_END,
    WRONG_PASSWORD,
    AUTHORIZATION_FAILED,
    REFRESH_TOKEN_FAILED,
    ACCOUNT_IS_NOT_AUTHORIZED,
    LOCATION_NAME_MUST_NOT_BE_EMPTY,
    LOCATION_DOES_NOT_EXIST,
    YOU_HAVE_NOT_RIGHTS_TO_DELETE_LOCATION,
    LOCATION_OWNER_NOT_FOUND,
    YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION,
    QUEUE_NAME_MUST_NOT_BE_EMPTY,
    QUEUE_DOES_NOT_EXIST,
    YOU_DO_NOT_HAVE_RIGHTS_TO_VIEW,
    CLIENT_DOES_NOT_HAVE_PHONE,
    PLEASE_GO_TO_SERVICE,
    ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_START,
    ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_END,
    USER_WITH_EMAIL_HAS_RIGHTS_IN_LOCATION_START,
    USER_WITH_EMAIL_HAS_RIGHTS_IN_LOCATION_END,
    USER_WITH_EMAIL_DOES_NOT_HAVE_RIGHTS_IN_LOCATION_START,
    USER_WITH_EMAIL_DOES_NOT_HAVE_RIGHTS_IN_LOCATION_END,
    WRONG_ACCESS_KEY,
    SPECIALIST_DOES_NOT_EXIST,
    LOCATION_CONTAINS_CLIENTS,
    CLIENT_ALREADY_ASSIGNED_TO_QUEUE,
    YOU_ARE_TRYING_TO_CREATE_SPECIALIST_WITH_SERVICES_THAT_ARE_NOT_IN_LOCATION,
    YOU_ARE_TRYING_TO_CREATE_SERVICES_SEQUENCE_WITH_SERVICES_THAT_ARE_NOT_IN_LOCATION,
    INCORRECT_REQUEST,
    CHOSEN_SERVICES_SEQUENCE_DOES_NOT_EXIST_IN_LOCATION,
    ONE_OR_MORE_OF_CHOSEN_SERVICES_DO_NOT_EXIST_IN_LOCATION,
    CLIENT_WITH_THIS_PHONE_ALREADY_EXIST,
    CLIENT_WITH_PHONE_RESERVED_START,
    CLIENT_WITH_PHONE_RESERVED_END,
    CLIENT_DOES_NOT_EXIST,
    CONFIRMATION_TIME_EXPIRED,
    YOUR_TICKET_NUMBER,
    YOU_CAN_CHECK_YOUR_STATUS_HERE,
    PLEASE_GO_TO_LINK_TO_CONFIRM_CONNECTION,
    CLIENT_ALREADY_CONFIRMED,
    CREATED_FROM_SPECIALIST_QUEUE_EXIST,
    INCORRECT_SERVICES,
    SERVICE_IS_BOOKED_BY_CLIENT,
    SERVICE_IS_ASSIGNED_TO_SPECIALIST,
    SERVICE_IS_ASSIGNED_TO_SERVICES_SEQUENCE,
    SERVICES_SEQUENCE_DOES_NOT_EXIST;

    public String toMessageId() {
        return name().toLowerCase();
    }
}
