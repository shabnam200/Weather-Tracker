package org.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import javax.swing.*;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WeatherAppTest {

    private WeatherApp weatherApp;

    @BeforeAll
    static void initAll() {
        System.out.println("Initializing Weather App Tests...");
    }

    @BeforeEach
    void setUp() {
        weatherApp = new WeatherApp() {};
    }

    @Test
    void testCityFieldExists() {
        assertNotNull(weatherApp.cityField, "City field should be initialized");
    }

    @Test
    void testWeatherLabelsNotNull() {
        assertNotNull(weatherApp.tempLabel, "Temperature label should be initialized");
        assertNotNull(weatherApp.weatherLabel, "Weather label should be initialized");
    }

    @ParameterizedTest
    @ValueSource(strings = {"London", "Paris", "Tokyo"})
    void testFetchWeatherWithValidCities(String city) {
        assertDoesNotThrow(() -> weatherApp.fetchWeather(URLEncoder.encode(city, StandardCharsets.UTF_8)), "Fetching weather should not throw an exception");
    }

    @ParameterizedTest
    @CsvSource({
            "New York, 5",
            "Los Angeles, 15",
            "Dubai, 35"
    })
    void testWeatherTemperatureDisplay(String city, int expectedTemp) {
        weatherApp.fetchWeather(URLEncoder.encode(city, StandardCharsets.UTF_8));
        assertNotNull(weatherApp.tempLabel.getText(), "Temperature label should be updated");
    }



    @Test
    @Disabled("Skipping test due to API rate limits")
    void testApiRateLimit() {
        fail("This test is disabled");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Test completed");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("All tests completed");
    }
}
