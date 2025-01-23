package com.atar.ticketBooking.controller;

import com.atar.ticketBooking.service.EmitterService;
import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/camunda")
public class CamundaController {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private EmitterService emitterService;

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

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String processInstanceKey) {
        return emitterService.addListener(processInstanceKey);
    }
}
