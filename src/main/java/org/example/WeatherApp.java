package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONObject;

public abstract class WeatherApp {
    private static final String API_KEY = "8a6db5d1f2e4b181f32fe33ada27c85f";

    private JFrame frame;
    JTextField cityField;
    JLabel tempLabel;
    JLabel weatherLabel;
    JLabel airQualityLabel;
    JLabel humidityLabel;
    JLabel windLabel;
    JLabel pressureLabel;
    JLabel visibilityLabel;
    private JPanel mainPanel;
    JPanel forecastPanel;

    public WeatherApp() {
        frame = new JFrame("Weather Forecast");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top search bar panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cityField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        topPanel.add(cityField);
        topPanel.add(searchButton);

        // Main weather info panel
        JPanel weatherPanel = new JPanel();
        weatherPanel.setLayout(new GridLayout(3, 2));
        weatherPanel.setBackground(new Color(176, 224, 230));
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Current Weather"));

        tempLabel = new JLabel("--°C", SwingConstants.CENTER);
        tempLabel.setFont(new Font("Arial", Font.BOLD, 40));
        weatherLabel = new JLabel("--", SwingConstants.CENTER);
        weatherLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        airQualityLabel = new JLabel("Air Quality: --");
        humidityLabel = new JLabel("Humidity: --");
        windLabel = new JLabel("Wind: --");
        pressureLabel = new JLabel("Pressure: --");
        visibilityLabel = new JLabel("Visibility: --");

        weatherPanel.add(tempLabel);
        weatherPanel.add(weatherLabel);
        weatherPanel.add(airQualityLabel);
        weatherPanel.add(humidityLabel);
        weatherPanel.add(windLabel);
        weatherPanel.add(pressureLabel);
        weatherPanel.add(visibilityLabel);

        // Forecast panel
        forecastPanel = new JPanel();
        forecastPanel.setLayout(new GridLayout(1, 7, 10, 10));
        forecastPanel.setBorder(BorderFactory.createTitledBorder("Next 5 Days →"));
        forecastPanel.setBackground(new Color(173, 216, 230));

        for (int i = 0; i < 7; i++) {
            JPanel dayPanel = new JPanel();
            dayPanel.setPreferredSize(new Dimension(100, 100));
            dayPanel.setBackground(new Color(135, 206, 250));
            dayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JLabel dayLabel = new JLabel("Day " + (i + 1));
            dayPanel.add(dayLabel);
            forecastPanel.add(dayPanel);
        }

        // Adding panels to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(weatherPanel, BorderLayout.CENTER);
        frame.add(forecastPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = cityField.getText();
                if (!city.isEmpty()) {
                    fetchWeather(city);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a city name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        fetchUserLocationWeather(); // Fetch weather for current location on startup

        frame.setVisible(true);
    }

    void fetchWeather(String city) {
        try {
            // Fetch weather details
            String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            // Handle HTTP error responses
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new Exception("404: City not found");
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new Exception("400: Bad Request");
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Unexpected error occurred: " + responseCode);
            }

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            // Check if API response is successful
            if (jsonResponse.getInt("cod") == 200) {
                String weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
                double temp = jsonResponse.getJSONObject("main").getDouble("temp");
                double humidity = jsonResponse.getJSONObject("main").getDouble("humidity");
                double wind = jsonResponse.getJSONObject("wind").getDouble("speed");
                double pressure = jsonResponse.getJSONObject("main").getDouble("pressure");
                double visibility = jsonResponse.has("visibility") ? jsonResponse.getDouble("visibility") / 1000 : -1;

                weatherLabel.setText("Weather: " + weather);
                tempLabel.setText(temp + "°C");
                humidityLabel.setText("Humidity: " + humidity + "%");
                windLabel.setText("Wind: " + wind + " mph");
                pressureLabel.setText("Pressure: " + pressure + " hPa");
                visibilityLabel.setText(visibility >= 0 ? "Visibility: " + visibility + " km" : "Visibility: --");

                // Get latitude & longitude for forecast
                double lat = jsonResponse.getJSONObject("coord").getDouble("lat");
                double lon = jsonResponse.getJSONObject("coord").getDouble("lon");

                // Fetch air quality
                fetchAirQuality(lat, lon);

                // Fetch 7-day forecast
                fetchForecast(lat, lon);
            } else {
                throw new Exception("Unexpected API response: " + jsonResponse.getInt("cod"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching weather data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    void fetchAirQuality(double lat, double lon) {
        try {
            String urlString = "http://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            int airQualityIndex = jsonResponse.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi");

            String airQualityDescription;
            switch (airQualityIndex) {
                case 1: airQualityDescription = "Good"; break;
                case 2: airQualityDescription = "Fair"; break;
                case 3: airQualityDescription = "Moderate"; break;
                case 4: airQualityDescription = "Poor"; break;
                case 5: airQualityDescription = "Very Poor"; break;
                default: airQualityDescription = "Unknown";
            }

            airQualityLabel.setText("Air Quality: " + airQualityDescription);
        } catch (Exception e) {
            airQualityLabel.setText("Air Quality: --");
            e.printStackTrace();
        }
    }


    private void fetchUserLocationWeather() {
        try {
            URL url = new URL("https://ipinfo.io/json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            String city = jsonResponse.getString("city");

            fetchWeather(city); // Fetch weather for detected city
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void fetchForecast(double lat, double lon) {
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat
                    + "&lon=" + lon
                    + "&units=metric&appid=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                JOptionPane.showMessageDialog(frame, "Error fetching forecast: " + responseCode, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            forecastPanel.removeAll();

            // Extract forecast data for the next 7 days
            JSONObject[] dailyData = new JSONObject[5];
            double[] dailyTemps = new double[5];
            String[] dailyWeather = new String[5];

            // Group data by day
            for (int i = 0; i < jsonResponse.getJSONArray("list").length(); i++) {
                JSONObject entry = jsonResponse.getJSONArray("list").getJSONObject(i);
                String date = entry.getString("dt_txt").split(" ")[0]; // Extract only the date
                int dayIndex = i / 8; // 8 entries per day (3-hour intervals)

                if (dayIndex < 5) {
                    dailyData[dayIndex] = entry;
                    dailyTemps[dayIndex] = entry.getJSONObject("main").getDouble("temp");
                    dailyWeather[dayIndex] = entry.getJSONArray("weather").getJSONObject(0).getString("description");
                }
            }

            // Update UI with 7-day forecast
            for (int i = 0; i < 5; i++) {
                JPanel dayPanel = new JPanel();
                dayPanel.setPreferredSize(new Dimension(100, 100));
                dayPanel.setBackground(new Color(135, 206, 250));
                dayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                JLabel dayLabel = new JLabel("Day " + (i + 1));
                JLabel tempLabel = new JLabel(dailyTemps[i] + "°C");
                JLabel descLabel = new JLabel(dailyWeather[i]);

                dayPanel.setLayout(new GridLayout(3, 1));
                dayPanel.add(dayLabel);
                dayPanel.add(tempLabel);
                dayPanel.add(descLabel);

                forecastPanel.add(dayPanel);
            }

            SwingUtilities.invokeLater(() -> {
                forecastPanel.revalidate();
                forecastPanel.repaint();
                frame.revalidate();
                frame.repaint();
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching forecast data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        new WeatherApp() {
            @Override
            protected InputStream getApiResponse(String apiUrl) {
                return null;
            }

            @Override
            protected void initializeUI() {

            }
        };
    }

    protected abstract InputStream getApiResponse(String apiUrl);

    protected abstract void initializeUI();
}


