package ru.tbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.tbank.controller.CurrencyController;
import ru.tbank.exception.BadRequestException;
import ru.tbank.exception.CurrencyNotFoundException;
import ru.tbank.exception.ServiceUnavailableException;
import ru.tbank.json.CurrencyConverterRequest;
import ru.tbank.json.CurrencyConverterResponse;
import ru.tbank.json.CurrencyRateResponse;
import ru.tbank.service.CurrencyService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CurrencyController.class})
class CurrencyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getCurrencyRateTest_ExistingCurrency() throws Exception {
        when(currencyService.getCurrencyRate("USD")).thenReturn(new CurrencyRateResponse("USD", 96.9483));

        mockMvc.perform(get("/currencies/rates/USD"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getCurrencyRateTest_NotExistingCurrency() throws Exception {
        when(currencyService.getCurrencyRate("XYZ")).thenThrow(new BadRequestException("Non-existent currency XYZ given"));

        mockMvc.perform(get("/currencies/rates/XYZ"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void getCurrencyRateTest_NotCbrCurrency() throws Exception {
        when(currencyService.getCurrencyRate("IRR")).thenThrow(new CurrencyNotFoundException("The currency IRR is not included in the list of the Central Bank of the Russian Federation"));

        mockMvc.perform(get("/currencies/rates/IRR"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void getCurrencyRateTest_ServiceUnavail() throws Exception {
        when(currencyService.getCurrencyRate("USD")).thenThrow(new ServiceUnavailableException("The service is unavailable, please try again later"));

        mockMvc.perform(get("/currencies/rates/USD"))
                .andExpect(status().isServiceUnavailable())
                .andDo(print());
    }


    @Test
    public void testConvertCurrency_Available() throws Exception {
        CurrencyConverterRequest convReq = new CurrencyConverterRequest("USD", "RUB", 100.5);
        CurrencyConverterResponse convResp = new CurrencyConverterResponse("USD", "RUB", 9743.30415);
        when(currencyService.convertCurrency(convReq)).thenReturn(convResp);

        mockMvc.perform(post("/currencies/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromCurrency\": \"USD\", \"toCurrency\": \"RUB\", \"amount\": 100.5}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(convResp)))
                .andDo(print());
    }
}