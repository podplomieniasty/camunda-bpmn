package com.atar.ticketBooking.worker;

import com.atar.ticketBooking.model.Showing;
import com.atar.ticketBooking.service.EmitterService;
import com.atar.ticketBooking.service.ReservationService;
import com.atar.ticketBooking.service.ShowingService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class CancellationProcessWorkers {

    @Autowired
    ReservationService reservationService;

    @Autowired
    ShowingService showingService;

    @Autowired
    private EmitterService emitterService;


    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;


    @JobWorker(type = "verify-access-code", autoComplete = true)
    public Map<String, Object> handleVerifyAccessCodeJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Verifying access code... ***");

        Map<String, Object> processVariables = job.getVariablesAsMap();
        String accessCode = (String) processVariables.get("accessCode");
        String email = (String) processVariables.get("email");

        boolean belongs = reservationService.doesAccessCodeBelongToEmail(accessCode, email);

        Map<String, Object> variables = new HashMap<>();
        variables.put("belongs", belongs);

        if (!belongs) {
            emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "INVALID_DATA");
        }

        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();

        System.out.println("*** Verification result: " + belongs +" ***");

        return variables;
    }

    @JobWorker(type = "check-movie-date", autoComplete = true)
    public Map<String, Object> handleCheckMovieDateJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Checking movie date... ***");

        Map<String, Object> processVariables = job.getVariablesAsMap();
        String accessCode = (String) processVariables.get("accessCode");

        try {
            Long showingId = reservationService.getShowingIdByAccessCode(accessCode);
            if (showingId == null) {
                throw new IllegalArgumentException("No reservation found for access code: " + accessCode);
            }

            Showing showing = showingService.getShowingById(showingId);
            if (showing == null) {
                throw new IllegalArgumentException("No showing found for id: " + showingId);
            }

            boolean lessThan24h = showingService.isShowingLessThan24HoursAway(showing);

            if (lessThan24h) {
                emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "RESERVATION_CANCELLED_FAILED");
            } else {
                emitterService.sendMessageToListener(String.valueOf(job.getProcessInstanceKey()), "RESERVATION_CANCELLED_SUCCESS");
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("less_than_24h", lessThan24h);

            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("*** Movie date check result - Less than 24h: " + lessThan24h + " ***");
            return variables;

        } catch (IllegalArgumentException e) {
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("ILLEGAL_ARGUMENT_ERROR")
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
            throw e;
        }
    }

    @JobWorker(type = "cancel-reservation", autoComplete = true)
    public Map<String, Object> handleCancelReservationJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Canceling reservation... ***");

        Map<String, Object> processVariables = job.getVariablesAsMap();
        String accessCode = (String) processVariables.get("accessCode");

        Map<String, Object> variables = new HashMap<>();

        try {
            boolean canceled = reservationService.cancelReservationByAccessCode(accessCode);

            variables.put("canceled", canceled);

            if (canceled) {
                System.out.println("*** Reservation with accessCode " + accessCode + " has been successfully canceled. ***");
            } else {
                throw new IllegalStateException("Failed to cancel reservation with accessCode: " + accessCode);
            }

            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("*** Error while canceling reservation: " + e.getMessage() + " ***");

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("CANCEL_RESERVATION_ERROR")
                    .errorMessage(e.getMessage())
                    .send()
                    .join();

            throw e;
        }

        return variables;
    }

    @JobWorker(type = "notify-cancel", autoComplete = true)
    public Map<String, Object> handleNotifyUserCancelJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Notifying user about successful cancellation... ***");

        Map<String, Object> variables = job.getVariablesAsMap();
        String email = (String) variables.get("email");

        if (email == null) {
            System.err.println("*** Email is missing in process variables. ***");

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("EMAIL_SENDING_ERROR")
                    .errorMessage("Email is missing in process variables.")
                    .send()
                    .join();

            return new HashMap<>();
        }

        try {
            sendConfirmationEmail(email);
        } catch (Exception e) {
            System.err.println("*** Failed to send confirmation email: " + e.getMessage() + " ***");

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("EMAIL_SENDING_ERROR")
                    .errorMessage("Failed to send confirmation email: " + e.getMessage())
                    .send()
                    .join();

            throw new RuntimeException(e);
        }

        client.newCompleteCommand(job.getKey())
                .send()
                .join();

        return new HashMap<>();
    }


    private void sendConfirmationEmail(String toAddress) throws MessagingException {
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

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(mailUsername);
        helper.setTo(toAddress);
        helper.setSubject("Potwierdzenie anulowania rezerwacji");
        helper.setText("Szanowny/a Użytkowniku,  \n\n" +
                "Twoja rezerwacja została pomyślnie anulowana. Jeśli masz dodatkowe pytania, prosimy o kontakt.\n\n" +
                "Pozdrawiamy,\nZespół Rezerwacji");

        mailSender.send(message);
        System.out.println("*** Email sent successfully to " + toAddress +" ***");
    }

    @JobWorker(type = "notify-failed-cancel", autoComplete = true)
    public Map<String, Object> handleNotifyUserFailedCancelJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Notifying user about failed cancellation... ***");

        Map<String, Object> variables = job.getVariablesAsMap();
        String email = (String) variables.get("email");

        if (email == null) {
            System.err.println("*** Email is missing in process variables. ***");

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("EMAIL_SENDING_ERROR")
                    .errorMessage("Email is missing in process variables.")
                    .send()
                    .join();

            return new HashMap<>();
        }

        try {
            sendEmail(email);
        } catch (Exception e) {
            System.err.println("*** Failed to send email: " + e.getMessage() + " ***");

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("EMAIL_SENDING_ERROR")
                    .errorMessage("Failed to send email: " + e.getMessage())
                    .send()
                    .join();

            throw new RuntimeException(e);
        }

        client.newCompleteCommand(job.getKey())
                .send()
                .join();

        return new HashMap<>();
    }

    private void sendEmail(String toAddress) throws MessagingException {
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

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(mailUsername);
        helper.setTo(toAddress);
        helper.setSubject("Błąd anulowania rezerwacji");
        helper.setText("Szanowny/a Użytkowniku,\n\n" +
                "Nie udało się anulować Twojej rezerwacji. Skontaktuj się z nami, aby uzyskać więcej informacji.\n\n" +
                "Pozdrawiamy,\nZespół Rezerwacji");

        mailSender.send(message);
        System.out.println("*** Email sent successfully to " + toAddress +" ***");
    }
}
