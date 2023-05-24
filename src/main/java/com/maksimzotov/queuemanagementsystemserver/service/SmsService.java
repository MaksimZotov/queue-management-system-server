package com.maksimzotov.queuemanagementsystemserver.service;

public interface SmsService {
    void send(String phoneNumber, String text);
}
