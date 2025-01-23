package com.atar.ticketBooking.worker;

import com.atar.ticketBooking.model.Showing;
import com.atar.ticketBooking.service.ReservationService;
import com.atar.ticketBooking.service.ShowingService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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

    @JobWorker(type = "notify-failed-cancel") // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleNotifyUserFailedCancelJob(JobClient client, ActivatedJob job) {
        System.out.println("Notifying user about failed cancelation...");

        // Zakończenie zadania bez dodatkowych zmiennych
        client.newCompleteCommand(job.getKey())
                .send()
                .join();

        return new HashMap<>();
    }
}
