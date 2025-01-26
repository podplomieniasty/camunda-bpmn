package com.atar.ticketBooking.worker;

import com.atar.ticketBooking.service.MovieService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
//public class FetchMoviesWorker {
//    @JobWorker(type = "fetch-movies") // Typ musi pasować do "Job Type" w BPMN
//    public void handleFetchMoviesJob(JobClient client, ActivatedJob job) {
//        // Logika biznesowa
//        System.out.println("Fetching movies...");
//        System.out.println("Job variables: " + job.getVariables());
//
//        // Zakończenie zadania
//        client.newCompleteCommand(job.getKey()).send().join();
//    }
//}

@Component
public class FetchMoviesWorker {

    @Autowired
    private MovieService movieService;

    @JobWorker(type = "fetch-movies", autoComplete = true) // Typ musi pasować do "Job Type" w BPMN
    public Map<String, Object> handleFetchMoviesJob(JobClient client, ActivatedJob job) {
        System.out.println("Fetching movies...");

        // Pobieranie listy filmów
        List<?> movies = movieService.getAllMovies();

        // Tworzenie zmiennej procesowej
        Map<String, Object> variables = new HashMap<>();
        variables.put("movies", movies);

        // Zakończenie zadania i przekazanie zmiennych procesu
        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();

        return variables;
    }
}
