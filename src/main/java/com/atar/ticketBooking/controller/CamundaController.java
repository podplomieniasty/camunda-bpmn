package com.atar.ticketBooking.controller;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/camunda")
public class CamundaController {

    @Autowired
    private ZeebeClient zeebeClient;

    private final String RESERVATION_PROCESS_ID = "reservation-process";

    @PostMapping("/start")
    public Map<String, Object> beginProcess(@RequestBody Map<String, Object> variables) {
        var event = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId(RESERVATION_PROCESS_ID)
                .latestVersion()
                .variables(variables)
                .send();
        variables.put("processInstanceKey", event.join().getProcessInstanceKey());
        return variables;
    }
}
