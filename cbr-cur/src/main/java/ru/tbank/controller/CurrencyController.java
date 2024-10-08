package ru.tbank.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.tbank.service.CurrencyService;
import ru.tbank.json.CurrencyConverterRequest;
import ru.tbank.json.CurrencyConverterResponse;
import ru.tbank.json.CurrencyRateResponse;
import ru.tbank.logging.LogExecutionTime;

@Slf4j
@LogExecutionTime
@RestController
@RequestMapping("/currencies")
public class CurrencyController {
    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/rates/{code}")
    public CurrencyRateResponse getCurrencyRate(@PathVariable String code) {
        return currencyService.getCurrencyRate(code);
    }

    @PostMapping("/convert")
    public CurrencyConverterResponse convertCurrency(@RequestBody CurrencyConverterRequest request) {
        return currencyService.convertCurrency(request);
    }
}
