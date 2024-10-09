package ru.tbank.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.tbank.exception.BadRequestException;
import ru.tbank.exception.CurrencyNotFoundException;
import ru.tbank.exception.ServiceUnavailableException;
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
    @Operation(summary = "Get currency rate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyConverterResponse.class),
                            examples = {@ExampleObject(value = "{\"currency\":\"CNY\",\"rate\":13.581}")})),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BadRequestException.class),
                            examples = {@ExampleObject(value = "{\"code\":400,\"message\":\"Non-existent currency XYZ given\"}")})),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CurrencyNotFoundException.class),
                    examples = {@ExampleObject(value = "{\"code\":404,\"message\":\"The currency XYZ is not included in the list of the Central Bank of the Russian Federation\"}")})),
            @ApiResponse(responseCode = "503", description = "Service unavailable", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ServiceUnavailableException.class),
                    examples = {@ExampleObject(value = "{\"status\":503,\"message\":\"The service is unavailable, please try again later\"}")}))
    })
    public CurrencyRateResponse getCurrencyRate(@PathVariable @NotBlank @NotNull String code) {
        return currencyService.getCurrencyRate(code);
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert currencies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyConverterResponse.class),
                            examples = {@ExampleObject(value = "{\"fromCurrency\":\"EUR\",\"toCurrency\":\"CNY\",\"convertedAmount\":77.8212944554893}")})),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BadRequestException.class),
                            examples = {@ExampleObject(value = "{\"code\":400,\"message\":\"Parameter amount is missing\"}")})),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CurrencyNotFoundException.class),
                    examples = {@ExampleObject(value = "{\"code\":404,\"message\":\"The currency XYZ is not included in the list of the Central Bank of the Russian Federation\"}")})),
            @ApiResponse(responseCode = "503", description = "Service unavailable", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ServiceUnavailableException.class),
                    examples = {@ExampleObject(value = "{\"status\":503,\"message\":\"The service is unavailable, please try again later\"}")}))
    })
    public CurrencyConverterResponse convertCurrency(@RequestBody @Valid CurrencyConverterRequest request) {
        return currencyService.convertCurrency(request);
    }
}
