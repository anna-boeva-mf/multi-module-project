package ru.tbank.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.client.CbrClient;
import ru.tbank.exception.BadRequestException;
import ru.tbank.exception.CurrencyNotFoundException;
import ru.tbank.json.CurrencyConverterRequest;
import ru.tbank.json.CurrencyConverterResponse;
import ru.tbank.json.CurrencyRateResponse;
import ru.tbank.logging.LogExecutionTime;
import ru.tbank.xml.CurrencyRate;
import java.util.Currency;
import java.util.List;

@Slf4j
@LogExecutionTime
@AllArgsConstructor
@Service
public class CurrencyService {
    private final CbrClient cbrClient;

    public CurrencyRateResponse getCurrencyRate(String code) {
        try {
            Currency.getInstance(code);
        } catch (IllegalArgumentException e) {
            log.error("Non-existent currency " + code + " given");
            throw new BadRequestException("Non-existent currency " + code + " given");
        }
        if (code.equals("RUB")) return new CurrencyRateResponse("RUB", 1.0);
        List<CurrencyRate> currencyRates = this.cbrClient.getCurrencyRates();
        CurrencyRateResponse response = currencyRates.stream()
                .filter(rate -> rate.getCode().equals(code))
                .findFirst()
                .map(rate -> new CurrencyRateResponse(rate.getCode(), Double.parseDouble(rate.getRate().replace(",", ".")))).orElse(null);
        if (response == null) {
            log.error("The currency " + code + " is not included in the list of the Central Bank of the Russian Federation");
            throw new CurrencyNotFoundException("The currency " + code + " is not included in the list of the Central Bank of the Russian Federation");
        }
        return response;
    }

    public CurrencyConverterResponse convertCurrency(CurrencyConverterRequest request) {
        String fromCurrency = request.getFromCurrency();
        String toCurrency = request.getToCurrency();
        Double amount = request.getAmount();
        if (fromCurrency == null) {
            log.error("Parameter fromCurrency is missing");
            throw new BadRequestException("Parameter fromCurrency is missing");
        }
        if (toCurrency == null) {
            log.error("Parameter toCurrency is missing");
            throw new BadRequestException("Parameter toCurrency is missing");
        }
        if (amount == null) {
            log.error("Parameter amount is missing");
            throw new BadRequestException("Parameter amount is missing");
        }
        if (amount <= 0) {
            log.error("Parameter amount must be greater than zero");
            throw new BadRequestException("Parameter amount must be greater than zero");
        }
        CurrencyRateResponse fromRate = getCurrencyRate(request.getFromCurrency());
        CurrencyRateResponse toRate = getCurrencyRate(request.getToCurrency());
        double convertedAmount = request.getAmount() * fromRate.getRate() / toRate.getRate();
        return new CurrencyConverterResponse(request.getFromCurrency(), request.getToCurrency(), convertedAmount);
    }
}
