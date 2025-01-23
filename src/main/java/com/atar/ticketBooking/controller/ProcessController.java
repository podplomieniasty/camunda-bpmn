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

    private static String BPMN_PROCESS_ID = "Process_0733ylc";

//    POTEMTY
//    @PostMapping("/start")
//    public void startProcessInstance(@RequestBody Map<String, Object> variables) {
//
//        LOG.info("Starting process " + BPMN_PROCESS_ID + " with variables: " + variables);
//
//        //variables.put("orderTotal", 200);
//
//        client
//                .newCreateInstanceCommand()
//                .bpmnProcessId(BPMN_PROCESS_ID)
//                .latestVersion()
//                .variables(variables)
//                .send();
//    }

//    @PostMapping("/start")
//    public String startProcess(@RequestParam String variable) {
//
//        LOG.info("Starting process " + BPMN_PROCESS_ID + " with variables: " + variable);
//
//        zeebeClient.newCreateInstanceCommand()
//                .bpmnProcessId(BPMN_PROCESS_ID) // Podaj ID procesu zdefiniowanego w BPMN
//                .latestVersion()
//                .variables(Map.of("variableName", variable)) // Przekazanie zmiennych do procesu
//                .send()
//                .join();
//
//        return "Process started successfully!";
//    }

//    @PostMapping("/start")
//    public String startProcess(@RequestParam String variable, Model model) {
//        // Uruchomienie procesu
//        var event = zeebeClient.newCreateInstanceCommand()
//                .bpmnProcessId("Process_0733ylc")
//                .latestVersion()
//                .variables(Map.of("variableName", variable))
//                .send()
//                .join();
//
//        // Pobierz processInstanceId z eventu
//        String processInstanceId = String.valueOf(event.getProcessInstanceKey());
//
//        // Zapisz processInstanceId w zmiennych procesu
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("processInstanceId", processInstanceId);
//
//        // Dodaj inne zmienne, które mogą być wymagane w procesie
//        variables.put("variableName", variable);
//
//        // Ponownie uruchom proces z nowymi zmiennymi, w tym processInstanceId
//        zeebeClient.newCreateInstanceCommand()
//                .bpmnProcessId("Process_0733ylc")
//                .latestVersion()
//                .variables(variables)
//                .send()
//                .join();
//
//        // Przekazanie processInstanceId do widoku
//        model.addAttribute("processInstanceId", processInstanceId);
//
//        return "select-movie-form"; // Przekazanie do widoku
//    }

    @PostMapping("/start")
    public String startProcess(@RequestParam String variable, HttpSession session) {
        // Uruchamianie procesu
        String processInstanceId = String.valueOf(zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_0733ylc")
                .latestVersion()
                .variables(Map.of("variableName", variable))
                .send()
                .join()
                .getProcessInstanceKey());

        // Zapisz processInstanceId w sesji
        session.setAttribute("processInstanceId", processInstanceId);

        return "Process started successfully!";
    }

//    @PostMapping("/start")
//    public String startProcess(@RequestParam String variable) {
//        // Uruchamianie procesu
//        var event = zeebeClient.newCreateInstanceCommand()
//                .bpmnProcessId("Process_0733ylc")
//                .latestVersion()
//                .variables(Map.of("variableName", variable))
//                .send()
//                .join();
//
//        // Pobierz processInstanceId z eventu
//        String processInstanceId = String.valueOf(event.getProcessInstanceKey());
//
//        // Zapisz processInstanceId w zmiennych procesu
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("processInstanceId", processInstanceId);
//
//        // Dodaj inne zmienne, które mogą być wymagane w procesie
//        variables.put("variableName", variable);
//
//        // Ponownie uruchom proces z nowymi zmiennymi, w tym processInstanceId
//        zeebeClient.newCreateInstanceCommand()
//                .bpmnProcessId("Process_0733ylc")
//                .latestVersion()
//                .variables(variables)
//                .send()
//                .join();
//
//        return "Process started successfully!";
//    }

}
