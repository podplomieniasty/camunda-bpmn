package com.atar.ticketBooking.worker;

import com.atar.ticketBooking.model.Reservation;
import com.atar.ticketBooking.service.CodeService;
import com.atar.ticketBooking.service.EmailService;
import com.atar.ticketBooking.service.EmitterService;
import com.atar.ticketBooking.service.ReservationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.camunda.feel.syntaxtree.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import scala.Int;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Component
@AllArgsConstructor
public class ReservationWorker {

    @Autowired private ReservationService reservationService;
    @Autowired private CodeService codeService;
    @Autowired private EmailService emailService;
    @Autowired private EmitterService emitterService;


    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @JobWorker(type = "verify-seat-availability", autoComplete = true)
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

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }

    @JobWorker(type = "verify-email", autoComplete = true)
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

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }

    @JobWorker(type = "reserve-seat", autoComplete = true)
    public Map<String, Object> reserveSeat(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Reserving seat");

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }

    @JobWorker(type = "generate-code", autoComplete = true)
    public Map<String, Object> generateCode(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Generating code");
        System.out.println(jobResultVariables);

        var code = codeService.generateCode(jobResultVariables.get("user_lname").toString(), jobResultVariables.get("movie_date").toString());

        System.out.println("*** Generated ticket code: "+ code +" ***");

        var reservation = new Reservation();

        reservation.setEmail(jobResultVariables.get("user_email").toString());
        reservation.setAccessCode(code);
        reservation.setShowingId(Long.valueOf(jobResultVariables.get("showingId").toString()));
        reservation.setSeatRow(Integer.parseInt(jobResultVariables.get("seatRow").toString()));
        reservation.setSeatCol(Integer.parseInt(jobResultVariables.get("seatCol").toString()));

        reservationService.addNewReservation(reservation);
        jobResultVariables.put("access_code", code);

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }


    @JobWorker(type = "send-to-email", autoComplete = true)
    public Map<String, Object> sendToEmail(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.debug", "true");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(mailUsername);
            helper.setTo(jobResultVariables.get("user_email").toString());
            helper.setSubject("Potwierdzenie rezerwacji");
            helper.setText("Szanowny/a "+ jobResultVariables.get("user_fname") +",  \n\n" +
                    "Twoja rezerwacja została pomyślnie przetworzona. Jeśli masz dodatkowe pytania, prosimy o kontakt.\n\n" +
                    "Kod dostępu: " + jobResultVariables.get("access_code") + "\n\n" +
                    "Pozdrawiamy,\nZespół Rezerwacji");

            mailSender.send(message);
        } catch (MessagingException me) {
            System.out.println("error");
        }
        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }

    @JobWorker(type = "notify-seat-not-available", autoComplete = true)
    public Map<String, Object> msgSeatNotAvailable(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Seat not available");
        emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "SEAT_NOT_AVAILABLE");

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }

    @JobWorker(type = "notify-invalid-email", autoComplete = true)
    public Map<String, Object> msgInvalidEmail(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Invalid e-mail");
        emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "INVALID_EMAIL");

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }

    @JobWorker(type = "notify-email-sent", autoComplete = true)
    public Map<String, Object> msgEmailSent(final JobClient client, final ActivatedJob job) {
        var jobResultVariables = job.getVariablesAsMap();
        System.out.println("Sending: Code sent");
        emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "EMAIL_SENT");

        client.newCompleteCommand(job.getKey())
                .variables(jobResultVariables)
                .send()
                .join();

        return jobResultVariables;
    }
}
