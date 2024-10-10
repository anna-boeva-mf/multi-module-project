package ru.tbank;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tbank.json.CurrencyConverterResponse;
import ru.tbank.json.CurrencyRateResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

public class CurrAppApiTest {

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost:8081";
    }

    @Test
    public void testValidEndpoint_CurrencyRate() {
        given()
                .when()
                .get("/currencies/rates/USD")
                .then()
                .statusCode(200);
    }

    @Test
    public void testValidEndpoint_CurrencyConvert() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"USD\", \"toCurrency\": \"RUB\", \"amount\": 100.5}")
                .post("/currencies/convert")
                .then()
                .statusCode(200);
    }

    @Test
    public void testCurrencyRate_FineResult() {
        CurrencyRateResponse resp = given().contentType(ContentType.JSON)
                .when()
                .get("/currencies/rates/VND")
                .as(CurrencyRateResponse.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(40.1143, resp.getRate(), "Check rate value from response"),
                () -> Assertions.assertEquals("VND", resp.getCurrency(), "Check currency code from response"));
    }

    @Test
    public void testCurrencyRate_RUB_Result() {
        CurrencyRateResponse resp = given()
                .when()
                .get("/currencies/rates/RUB")
                .as(CurrencyRateResponse.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1.0, resp.getRate(), "Check RUB rate value from response"),
                () -> Assertions.assertEquals("RUB", resp.getCurrency(), "Check RUB currency code from response"));
    }

    @Test
    public void testCurrencyRate_VoidCurrency() {
        given()
                .when()
                .get("/currencies/rates/")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCurrencyConvert_FineResult() {
        CurrencyConverterResponse resp = given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"EGP\", \"toCurrency\": \"NOK\", \"amount\": 100.0}")
                .post("/currencies/convert")
                .as(CurrencyConverterResponse.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(21.937723175817265, resp.getConvertedAmount(), "Check converted amount from response"),
                () -> Assertions.assertEquals("EGP", resp.getFromCurrency(), "Check 'from' currency code from response"),
                () -> Assertions.assertEquals("NOK", resp.getToCurrency(), "Check 'to' currency code from response"));
    }

    @Test
    public void testCurrencyConvert_1to1() {
        CurrencyConverterResponse resp = given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"TJS\", \"toCurrency\": \"TJS\", \"amount\": 1.0}")
                .post("/currencies/convert")
                .as(CurrencyConverterResponse.class);
        Assertions.assertEquals(1.0, resp.getConvertedAmount());
    }

    @Test
    public void testCurrencyConvert_NegatAmount() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"TJS\", \"toCurrency\": \"TJS\", \"amount\": -1.0}")
                .post("/currencies/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Parameter amount must be greater than zero"));
    }

    @Test
    public void testCurrencyConvert_VoidFromCurrency() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"toCurrency\": \"NOK\", \"amount\": 100.0}")
                .post("/currencies/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Parameter fromCurrency is missing"));
    }

    @Test
    public void testCurrencyConvert_VoidToCurrency() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"EGP\", \"amount\": 100.0}")
                .post("/currencies/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Parameter toCurrency is missing"));
    }

    @Test
    public void testCurrencyConvert_VoidAmount() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"EGP\",\"toCurrency\": \"NOK\"}")
                .post("/currencies/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Parameter amount is missing"));
    }

    @Test
    public void testCurrencyConvert_Void() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{}")
                .post("/currencies/convert")
                .then()
                .statusCode(400);
    }

    @Test
    public void testCurrencyConvert_NonExistCurrency() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"DDD\", \"toCurrency\": \"TJS\", \"amount\": 1.0}")
                .post("/currencies/convert")
                .then()
                .statusCode(400)
                .body("message", equalTo("Non-existent currency DDD given"));
    }

    @Test
    public void testCurrencyConvert_NotCbrCurrency() {
        given()
                .when().contentType(ContentType.JSON)
                .body("{\"fromCurrency\": \"TJS\", \"toCurrency\": \"CHE\", \"amount\": 1.0}")
                .post("/currencies/convert")
                .then()
                .statusCode(404)
                .body("message", equalTo("The currency CHE is not included in the list of the Central Bank of the Russian Federation"));
    }

}
