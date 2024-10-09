package ru.tbank.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CurrencyConverterRequest {
    @Schema(description = "Convert from currency code", example = "USD")
    @NotBlank
    @JsonProperty("fromCurrency")
    private String fromCurrency;

    @Schema(description = "Convert to currency code", example = "EUR")
    @NotBlank
    @JsonProperty("toCurrency")
    private String toCurrency;

    @Schema(description = "Amount to convert", example = "100.2")
    @NotBlank
    @NotNull
    @JsonProperty("amount")
    private Double amount;

}
