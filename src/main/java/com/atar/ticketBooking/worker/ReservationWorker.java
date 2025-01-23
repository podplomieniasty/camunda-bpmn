package com.atar.ticketBooking.worker;

import com.atar.ticketBooking.model.Reservation;
import com.atar.ticketBooking.service.CodeService;
import com.atar.ticketBooking.service.EmailService;
import com.atar.ticketBooking.service.EmitterService;
import com.atar.ticketBooking.service.ReservationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.AllArgsConstructor;
import org.camunda.feel.syntaxtree.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.Int;

import java.util.Map;
import java.util.Objects;

@Component
@AllArgsConstructor
public class ReservationWorker {

    @Autowired private ReservationService reservationService;
    @Autowired private CodeService codeService;
    @Autowired private EmailService emailService;
    @Autowired private EmitterService emitterService;

    @JobWorker(type = "verify-seat-availability")
    public Map<String, Object> verifySeatAvailability(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Verifying seat availability");
        System.out.println(jobResultVariables);

        var splitt = jobResultVariables.get("movie_seat").toString().replace("C","").replace("R", "").split("-");
        Integer col = Integer.parseInt(splitt[0]);
        Integer row = Integer.parseInt(splitt[1]);

        // service to do this shit goes here
        boolean isSeatFree = reservationService.isReservationAvailable(
                Long.valueOf(jobResultVariables.get("showingId").toString()),
                row,
                col
        );
        System.out.println("SEAT IS AVAILABLE: " + isSeatFree);
        jobResultVariables.put("isSeatFree", isSeatFree);
        jobResultVariables.put("seatRow", row);
        jobResultVariables.put("seatCol", col);

        if(!isSeatFree) {
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
        var email = (String)jobResultVariables.get("user_email");

        if(Objects.isNull(email) || email.isEmpty()) {
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("INVALID_EMAIL")
                    .send()
                    .join();
            System.out.println("EMAIL IS EMPTY");

        }
        if(!email.contains("@")) {
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("INVALID_EMAIL")
                    .send()
                    .join();
            System.out.println("EMAIL DOESNT CONTAIN @");
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
        var code = codeService.generateCode(jobResultVariables.get("user_lname").toString(), jobResultVariables.get("movie_date").toString());
        jobResultVariables.put("access_code", code);
        return jobResultVariables;
    }

    @JobWorker(type = "send-to-email")
    public Map<String, Object> sendToEmail(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending e-mail");
        emailService.sendReservationCodeEmail(jobResultVariables.get("user_email").toString());
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
        emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "INVALID_EMAIL");
        return jobResultVariables;
    }

    @JobWorker(type = "notify-email-sent")
    public Map<String, Object> msgEmailSent(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Code sent");

        return jobResultVariables;
    }
}
