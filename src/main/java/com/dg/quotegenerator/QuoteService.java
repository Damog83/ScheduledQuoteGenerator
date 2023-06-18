package com.dg.quotegenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

@Service
public class QuoteService {

    private static final int maxTries = 3;
    private int currentTries = 0;
    private final WebClient webClient;
    private final ScheduledExecutorService executorService;

    @Autowired
    public QuoteService(WebClient webClient) {
        this.webClient = webClient;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Scheduled(fixedRate = 30000)
    public void getQuote() {
        while (currentTries < maxTries) {
            try {
                List<Quote> quotes = webClient.get()
                        .uri("https://zenquotes.io/api/random")
                        .retrieve()
                        .bodyToFlux(Quote.class)
                        .collectList()
                        .block(Duration.ofSeconds(10));

                if (quotes != null && !quotes.isEmpty()) {
                    String quote = quotes.get(0).q();
                    String author = quotes.get(0).a();
                    System.out.println("Quote: " + quote);
                    System.out.println("Author: " + author);
                    return;
                }
            } catch (WebClientException | NullPointerException e) {
                System.out.println(e.getMessage());
            }

            currentTries++;
            System.out.println("No quote received from API on attempt: " + currentTries + ". Please wait a moment for another attempt");
            if (currentTries < maxTries) {
                long delay = currentTries * 1000L;
                executorService.schedule(this::getQuote, delay, TimeUnit.MILLISECONDS);
            }

        }
            executorService.shutdown();
            System.out.println("Sorry failed after " + currentTries + " attempts.");
    }
}
