package org.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

class WeatherAppTest {
    private WeatherApp weatherApp;

    @BeforeEach
    void setUp() {
        // Mocking the abstract method getApiResponse
        weatherApp = new WeatherApp() {
            @Override
            protected InputStream getApiResponse(String apiUrl) {
                return new ByteArrayInputStream(mockApiResponse(apiUrl).getBytes());
            }

            @Override
            protected void initializeUI() {
                // Initializing components for testing (mock initialization)
                weatherLabel = new JLabel();
                tempLabel = new JLabel();
                humidityLabel = new JLabel();
                windLabel = new JLabel();
                airQualityLabel = new JLabel();
                forecastPanel = new JPanel(); // Mocking the forecastPanel
            }
        };
    }

    @Test
    void testFetchWeather_ValidCity() throws Exception {
        // Use invokeAndWait to ensure UI updates happen on the EDT
        SwingUtilities.invokeAndWait(() -> {
            weatherApp.fetchWeather("London");
        });
        Thread.sleep(500);
        // Verify UI updates with correct mock data
        assertEquals("Weather: scattered clouds", weatherApp.weatherLabel.getText());
        assertEquals("5.51°C", weatherApp.tempLabel.getText());
        assertEquals("Humidity: 84.0%", weatherApp.humidityLabel.getText());
        assertEquals("Wind: 2.68 mph", weatherApp.windLabel.getText());
    }

    @Test
    void testFetchAirQuality_ValidLocation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            weatherApp.fetchAirQuality(51.5074, -0.1278);
        });

        // Verify air quality label text
        assertEquals("Air Quality: Fair", weatherApp.airQualityLabel.getText());
    }

    @Test
    void testFetchForecast_ValidLocation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            weatherApp.fetchForecast(51.5074, -0.1278);
        });

        // Verify that a component is added to the forecast panel (just checking for non-null here)
        assertNotNull(weatherApp.forecastPanel.getComponent(0));
    }

    private String mockApiResponse(String apiUrl) {
        if (apiUrl.contains("weather")) {
            return "{ \"cod\": 200, \"weather\": [{\"description\": \"scattered clouds\"}], "
                    + "\"main\": { \"temp\": 20.5, \"humidity\": 60 }, \"wind\": { \"speed\": 5.2 }, "
                    + "\"coord\": { \"lat\": 51.5074, \"lon\": -0.1278 } }";
        } else if (apiUrl.contains("air_pollution")) {
            return "{ \"list\": [{ \"main\": { \"aqi\": 2 } }] }";  // AQI 2 corresponds to 'Fair'
        }
        return "{}";
    }
}
