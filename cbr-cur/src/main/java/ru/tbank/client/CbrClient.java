package ru.tbank.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.tbank.exception.ServiceUnavailableException;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.xml.CurrencyRate;
import ru.tbank.xml.ValCurs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@LogExecutionTime
@Component
public class CbrClient {

    private final RestTemplate restTemplate;

    @Value("${cbrf.url}")
    private String cbrfUrl;

    public CbrClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("currencyRates")
    @CircuitBreaker(name = "cbr-client", fallbackMethod = "fallbackGetCurrencyRates")
    public List<CurrencyRate> getCurrencyRates() {
        log.info("Getting a list of exchange rates from the Central Bank service");
        ValCurs valCurs = restTemplate.getForObject(cbrfUrl + "?date_req=" + new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()), ValCurs.class);
        return valCurs.getValutes().stream()
                .map(valute -> new CurrencyRate(valute.getCharCode(), valute.getValue()))
                .collect(Collectors.toList());
    }

    private List<CurrencyRate> fallbackGetCurrencyRates(Throwable throwable) throws Throwable {
        log.error("Problem with getting list of exchange rates from the service: {}", throwable.getMessage());
        if (throwable instanceof CallNotPermittedException) {
            throw new ServiceUnavailableException("The service is unavailable, please try again later");
        } else {
            throw throwable;
        }
    }
}
