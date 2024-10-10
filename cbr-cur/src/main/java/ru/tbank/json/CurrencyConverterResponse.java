package ru.tbank.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrencyConverterResponse {

    @JsonProperty("fromCurrency")
    private String fromCurrency;

    @JsonProperty("toCurrency")
    private String toCurrency;

    @JsonProperty("convertedAmount")
    private Double convertedAmount;
}
