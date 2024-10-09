package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tbank.exception.BadRequestException;
import ru.tbank.exception.CurrencyNotFoundException;
import ru.tbank.json.CurrencyConverterRequest;
import ru.tbank.json.CurrencyConverterResponse;
import ru.tbank.json.CurrencyRateResponse;
import ru.tbank.xml.CurrencyRate;
import ru.tbank.xml.ValCurs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CurrencyService {
    private final RestTemplate restTemplate;

    @Value("${cbrf.url}")
    private String cbrfUrl;

    @Autowired
    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("currencyRates")
    public List<CurrencyRate> getCurrencyRates() {
        ValCurs valCurs = restTemplate.getForObject(cbrfUrl + "?date_req=" + new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()), ValCurs.class);
        return valCurs.getValutes().stream()
                .map(valute -> new CurrencyRate(valute.getCharCode(), valute.getValue()))
                .collect(Collectors.toList());
    }

    @Cacheable("currencyRate")
    public CurrencyRateResponse getCurrencyRate(String code) {
        try {
            Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Non-existent currency given");
        }
        if (code.equals("RUB")) return new CurrencyRateResponse("RUB", 1.0);
        List<CurrencyRate> currencyRates = getCurrencyRates();
        CurrencyRateResponse response = currencyRates.stream()
                .filter(rate -> rate.getCode().equals(code))
                .findFirst()
                .map(rate -> new CurrencyRateResponse(rate.getCode(), Double.parseDouble(rate.getRate().replace(",", ".")))).orElse(null);
        if (response == null) throw new CurrencyNotFoundException("The currency is not included in the list of the Central Bank of the Russian Federation");
        return response;
    }

    public CurrencyConverterResponse convertCurrency(CurrencyConverterRequest request) {
        String fromCurrency = request.getFromCurrency();
        String toCurrency = request.getToCurrency();
        Double amount = request.getAmount();
        if (fromCurrency == null) throw new BadRequestException("Parameter fromCurrency is missing");
        if (toCurrency == null) throw new BadRequestException("Parameter toCurrency is missing");
        if (amount == null) throw new BadRequestException("Parameter amount is missing");
        CurrencyRateResponse fromRate = getCurrencyRate(request.getFromCurrency());
        CurrencyRateResponse toRate = getCurrencyRate(request.getToCurrency());
        double convertedAmount = request.getAmount() * fromRate.getRate() / toRate.getRate();
        return new CurrencyConverterResponse(request.getFromCurrency(), request.getToCurrency(), convertedAmount);
    }
}
