package ru.tbank.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRateResponse {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("rate")
    private BigDecimal rate;
}
