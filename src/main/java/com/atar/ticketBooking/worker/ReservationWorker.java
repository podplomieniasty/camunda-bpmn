package com.atar.ticketBooking.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class ReservationWorker {

    @JobWorker(type = "verify-seat-availability")
    public Map<String, Object> verifySeatAvailability(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Verifying seat availability");
        System.out.println(jobResultVariables);

        // service to do this shit goes here
        boolean isVerified = true;
        jobResultVariables.put("seatIsVerified", isVerified);

        if(!isVerified) {
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("SEAT_NOT_AVAILABLE")
                    .send()
                    .join();
        }
        return jobResultVariables;
    }

    @JobWorker(type = "verify-email")
    public Map<String, Object> verifyEmail(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();

        System.out.println("Verifying e-mail integrity");
        var email = (String)jobResultVariables.get("email");

        if(Objects.isNull(email) || email.isEmpty()) {
//            client.newThrowErrorCommand(job.getKey())
//                    .errorCode("SEAT_NOT_AVAILABLE")
//                    .send()
//                    .join();
        }
        return jobResultVariables;
    }

    @JobWorker(type = "reserve-seat")
    public Map<String, Object> reserveSeat(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Reserving seat");


        return jobResultVariables;
    }

    @JobWorker(type = "generate-code")
    public Map<String, Object> generateCode(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Generating code");

        return jobResultVariables;
    }

    @JobWorker(type = "send-to-email")
    public Map<String, Object> sendToEmail(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending e-mail");

        return jobResultVariables;
    }

    @JobWorker(type = "notify-seat-not-available")
    public Map<String, Object> msgSeatNotAvailable(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Seat not available");

        return jobResultVariables;
    }

    @JobWorker(type = "notify-invalid-email")
    public Map<String, Object> msgInvalidEmail(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Invalid e-mail");

        return jobResultVariables;
    }

    @JobWorker(type = "notify-email-sent")
    public Map<String, Object> msgEmailSent(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Code sent");

        return jobResultVariables;
    }
}
