package com.atar.ticketBooking.controller;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ZeebeClient zeebeClient;

    @PostMapping("/start")
    public String startProcess(@RequestParam String variable, HttpSession session) {
        String processInstanceId = String.valueOf(zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_0733ylc")
                .latestVersion()
                .variables(Map.of("variableName", variable))
                .send()
                .join()
                .getProcessInstanceKey());

        session.setAttribute("processInstanceId", processInstanceId);

        return "Process started successfully!";
    }

    @PostMapping("/cancel-reservation")
    public String cancelReservationProcess(@RequestBody Map<String, String> requestBody) {
        String accessCode = requestBody.get("accessCode");
        String email = requestBody.get("email");

        String processInstanceId = String.valueOf(zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_cancel_reservation")
                .latestVersion()
                .variables(Map.of(
                        "accessCode", accessCode,
                        "email", email
                ))
                .send()
                .join()
                .getProcessInstanceKey());

        System.out.println("*** Run process Cancel Reservation in instance with id: " + processInstanceId + " ***");

        return "Process started successfully with instance ID: " + processInstanceId;
    }
}
