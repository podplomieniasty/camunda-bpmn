package com.atar.ticketBooking.component;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DeployProcess implements CommandLineRunner {

    @Autowired
    private ZeebeClient zeebeClient;

    private final String RESERVATION_PROCESS_FILE = "reservation_process_2.bpmn";
    private final String CANCEL_RESERVATION_PROCESS_FILE = "Cancel-reservation.bpmn";

    @Override
    public void run(String... args) throws Exception {
        zeebeClient.newDeployCommand()
                .addResourceFromClasspath(RESERVATION_PROCESS_FILE) // Ścieżka do Twojego BPMN
                .addResourceFromClasspath(CANCEL_RESERVATION_PROCESS_FILE)
                .send()
                .join();

        System.out.println("*** Successfully deployed process: " + RESERVATION_PROCESS_FILE +" ***");
        System.out.println("*** Successfully deployed process: " + CANCEL_RESERVATION_PROCESS_FILE +" ***");
    }
}
