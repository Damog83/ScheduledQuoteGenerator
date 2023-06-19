package com.dg.quotegenerator;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class QuoteGeneratorApplicationTests {

    private WebClient webClient;
    private WebClient.ResponseSpec responseSpec;
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    private QuoteService quoteService;
    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
   // @Autowired
    public void setup() {
        webClient = Mockito.mock(WebClient.class);
        requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        Mockito.<WebClient.RequestHeadersUriSpec<?>>when(webClient.get()).thenReturn(requestHeadersUriSpec);
        Mockito.<WebClient.RequestHeadersSpec<?>>when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        quoteService = new QuoteService(webClient);

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void cleanup() {
        System.setOut(originalSystemOut);
        Mockito.reset(webClient, requestHeadersUriSpec, requestHeadersSpec, responseSpec);
    }
    @Test
    @DisplayName("Test the quote service returns a quote and author")
    public void checkSuccessfulGetQuote() {

        List<Quote> expectedQuotes = Arrays.asList(new Quote("Test Quote", "Test Author", "Test Url", "1", "Test Html"));
        Mockito.when(responseSpec.bodyToFlux(Quote.class)).thenReturn(Flux.fromIterable(expectedQuotes));

        quoteService.getQuote();

        Mockito.verify(webClient).get();
        Mockito.verify(requestHeadersUriSpec).uri("https://zenquotes.io/api/random");
        Mockito.verify(requestHeadersSpec).retrieve();
        Mockito.verify(responseSpec).bodyToFlux(Quote.class);

        String consoleOutput = outputStream.toString().trim();

        Assertions.assertTrue(consoleOutput.contains("Quote: Test Quote"));
        Assertions.assertTrue(consoleOutput.contains("Author: Test Author"));
    }

    @Test
    @DisplayName("Tests when an empty response is given from Api")
    public void checkEmptyResponseFromApi() throws InterruptedException {
        Mockito.when(responseSpec.bodyToFlux(Quote.class)).thenReturn(Flux.empty());
        quoteService.getQuote();

        Mockito.verify(webClient, Mockito.times(3)).get();
        Mockito.verify(requestHeadersUriSpec, Mockito.times(3)).uri("https://zenquotes.io/api/random");
        Mockito.verify(requestHeadersSpec, Mockito.times(3)).retrieve();
        Mockito.verify(responseSpec, Mockito.times(3)).bodyToFlux(Quote.class);

            String consoleOutput = outputStream.toString().trim();

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                Assertions.assertTrue(consoleOutput.contains("No quote received from API on attempt: 1. Please wait a moment for another attempt"))
        );

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                Assertions.assertTrue(consoleOutput.contains("No quote received from API on attempt: 2. Please wait a moment for another attempt"))
        );

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                Assertions.assertTrue(consoleOutput.contains("Sorry failed after 3 attempts."))
        );
    }

    @Test
    @DisplayName("Tests when a null response is given from Api")
    public void checkNullResponseFromApi() throws InterruptedException {
        Mockito.when(responseSpec.bodyToFlux(Quote.class)).thenReturn(null);
        quoteService.getQuote();

        Mockito.verify(webClient, Mockito.times(3)).get();
        Mockito.verify(requestHeadersUriSpec, Mockito.times(3)).uri("https://zenquotes.io/api/random");
        Mockito.verify(requestHeadersSpec, Mockito.times(3)).retrieve();
        Mockito.verify(responseSpec, Mockito.times(3)).bodyToFlux(Quote.class);


        String consoleOutput = outputStream.toString().trim();


        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                Assertions.assertTrue(consoleOutput.contains("No quote received from API on attempt: 1. Please wait a moment for another attempt"))
        );

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                Assertions.assertTrue(consoleOutput.contains("No quote received from API on attempt: 2. Please wait a moment for another attempt"))
        );

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                Assertions.assertTrue(consoleOutput.contains("Sorry failed after 3 attempts."))
        );
   }
}
