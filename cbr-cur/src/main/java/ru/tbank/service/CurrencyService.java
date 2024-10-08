package ru.tbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.tbank.json.CurrencyConverterRequest;
import ru.tbank.json.CurrencyConverterResponse;
import ru.tbank.json.CurrencyRateResponse;
import ru.tbank.xml.CurrencyRate;
import ru.tbank.xml.ValCurs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        List<CurrencyRate> currencyRates = getCurrencyRates();
        return currencyRates.stream()
                .filter(rate -> rate.getCode().equals(code))
                .findFirst()
                .map(rate -> new CurrencyRateResponse(rate.getCode(), Double.parseDouble(rate.getRate().replace(",", "."))))
                .orElseThrow();
    }

    public CurrencyConverterResponse convertCurrency(CurrencyConverterRequest request) {
        CurrencyRateResponse fromRate = getCurrencyRate(request.getFromCurrency());
        CurrencyRateResponse toRate = getCurrencyRate(request.getToCurrency());
        if (fromRate == null || toRate == null) {
            throw new RuntimeException("Валютный курс не найден");
        }
        double convertedAmount = request.getAmount() * fromRate.getRate() / toRate.getRate();
        return new CurrencyConverterResponse(request.getFromCurrency(), request.getToCurrency(), convertedAmount);
    }
}
