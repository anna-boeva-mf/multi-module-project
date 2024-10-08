package ru.tbank.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class CurrencyRateResponse {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("rate")
    private Double rate;
}
