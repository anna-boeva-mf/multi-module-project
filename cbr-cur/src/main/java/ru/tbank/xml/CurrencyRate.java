package ru.tbank.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class CurrencyRate {
    @JacksonXmlProperty(localName = "CharCode")
    private String code;

    @JacksonXmlProperty(localName = "Value")
    private String rate;
}
