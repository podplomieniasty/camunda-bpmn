package com.atar.ticketBooking.worker;

import com.atar.ticketBooking.model.Showing;
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

    @JobWorker(type = "verify-access-code") // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleVerifyAccessCodeJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Verifying access code... ***");

        Map<String, Object> processVariables = job.getVariablesAsMap();
        String accessCode = (String) processVariables.get("accessCode");
        String email = (String) processVariables.get("email");

        boolean belongs = reservationService.doesAccessCodeBelongToEmail(accessCode, email);

        Map<String, Object> variables = new HashMap<>();
        variables.put("belongs", belongs); // Ustawienie zmiennej procesu na true/false

        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();

        System.out.println("*** Verification result: " + belongs +" ***");

        return variables;
    }

    @JobWorker(type = "check-movie-date") // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleCheckMovieDateJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Checking movie date... ***");

        // Pobieranie zmiennych procesu
        Map<String, Object> processVariables = job.getVariablesAsMap();
        String accessCode = (String) processVariables.get("accessCode");

        // Pobranie `showingId` na podstawie `accessCode`
        Long showingId = reservationService.getShowingIdByAccessCode(accessCode);
        if (showingId == null) {
            throw new IllegalArgumentException("No reservation found for access code: " + accessCode);
        }

        // Pobranie seansu na podstawie `showingId`
        Showing showing = showingService.getShowingById(showingId);
        if (showing == null) {
            throw new IllegalArgumentException("No showing found for id: " + showingId);
        }

        // Sprawdzenie daty i godziny seansu
        boolean lessThan24h = showingService.isShowingLessThan24HoursAway(showing);

        // Tworzenie zmiennej procesowej
        Map<String, Object> variables = new HashMap<>();
        variables.put("less_than_24h", lessThan24h);

        // Zakończenie zadania i przekazanie zmiennych procesu
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();

        System.out.println("*** Movie date check result - Less than 24h: " + lessThan24h +" ***");

        return variables;
    }


    @JobWorker(type = "cancel-reservation") // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleCancelReservationJob(JobClient client, ActivatedJob job) {
        System.out.println("*** Canceling reservation... ***");

        // Pobieranie zmiennych procesu
        Map<String, Object> processVariables = job.getVariablesAsMap();
        String accessCode = (String) processVariables.get("accessCode");

        // Tworzenie zmiennej procesowej
        Map<String, Object> variables = new HashMap<>();

        try {
            // Usunięcie rezerwacji na podstawie accessCode
            boolean canceled = reservationService.cancelReservationByAccessCode(accessCode);

            // Ustawienie zmiennej procesowej na true, jeśli rezerwacja została anulowana
            variables.put("canceled", canceled);

            if (canceled) {
                System.out.println("*** Reservation with accessCode " + accessCode + " has been successfully canceled. ***");
            } else {
                System.out.println("*** Failed to cancel reservation with accessCode " + accessCode + ". ***");
            }
        } catch (Exception e) {
            System.err.println("*** Error while canceling reservation: " + e.getMessage() + " ***");
            variables.put("canceled", false);
        }

        // Zakończenie zadania i przekazanie zmiennych procesu
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();

        return variables;
    }

    @JobWorker(type = "notify-cancel") // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleNotifyUserCancelJob(JobClient client, ActivatedJob job) {
        System.out.println("Notifying user about cancelation...");

        // Zakończenie zadania bez dodatkowych zmiennych
        client.newCompleteCommand(job.getKey())
                .send()
                .join();

        return new HashMap<>();
    }

//    @JobWorker(type = "notify-failed-cancel") // Typ musi pasować do "Job Type" w BPMN
//    public Map<String, Object> handleNotifyUserFailedCancelJob(JobClient client, ActivatedJob job) {
//        System.out.println("Notifying user about failed cancelation...");
//
//        // Zakończenie zadania bez dodatkowych zmiennych
//        client.newCompleteCommand(job.getKey())
//                .send()
//                .join();
//
//        return new HashMap<>();
//    }

    @JobWorker(type = "notify-failed-cancel") // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleNotifyUserFailedCancelJob(JobClient client, ActivatedJob job) {
        System.out.println("Notifying user about failed cancellation...");

        Map<String, Object> variables = job.getVariablesAsMap();
        String email = (String) variables.get("email");
//        String firstName = (String) variables.get("firstName");
        String firstName = "Użytkowniku";

        if (email == null ) {
            System.err.println("Email is missing in process variables.");
            client.newCompleteCommand(job.getKey()).send().join();
            return new HashMap<>();
        }

        try {
            sendEmail(email, firstName);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        client.newCompleteCommand(job.getKey()).send().join();
        return new HashMap<>();
    }

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    private void sendEmail(String toAddress, String firstName) throws MessagingException {
        // Konfiguracja JavaMailSender
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

        // Tworzenie wiadomości
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(mailUsername);
        helper.setTo(toAddress);
        helper.setSubject("Niepowodzenie anulowania rezerwacji");
        helper.setText("Szanowny/a " + firstName + ",\n\n" +
                "Nie udało się anulować Twojej rezerwacji. Skontaktuj się z nami, aby uzyskać więcej informacji.\n\n" +
                "Pozdrawiamy,\nZespół Rezerwacji");

        // Wysyłanie wiadomości
        mailSender.send(message);
        System.out.println("Email sent successfully to " + toAddress);
    }
}
