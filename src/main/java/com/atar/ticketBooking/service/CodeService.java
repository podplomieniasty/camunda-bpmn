package com.atar.ticketBooking.service;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class CodeService {

    public String generateCode(String lname, String movie_date) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        var code = timestamp.toString() + '-' + lname + '-' + movie_date;
        code = code.replace(" ", "-");
        return code;
    }
}
