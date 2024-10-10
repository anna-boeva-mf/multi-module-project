package ru.tbank.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRateResponse {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("rate")
    private Double rate;
}
