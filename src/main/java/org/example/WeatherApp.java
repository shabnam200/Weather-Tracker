import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONObject;

public class WeatherApp {
    private static final String API_KEY = "8a6db5d1f2e4b181f32fe33ada27c85f";

    private JFrame frame;
    private JTextField cityField;
    private JLabel tempLabel, weatherLabel, airQualityLabel, humidityLabel, windLabel, pressureLabel, visibilityLabel;
    private JPanel mainPanel, forecastPanel;

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
        forecastPanel.setBorder(BorderFactory.createTitledBorder("Next 7 Days →"));
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

    private void fetchWeather(String city) {
        try {
            // Fetch weather details
            String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
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
                JOptionPane.showMessageDialog(frame, "City not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching weather data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchAirQuality(double lat, double lon) {
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
    private void fetchForecast(double lat, double lon) {
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat
                    + "&lon=" + lon
                    + "&exclude=current,minutely,hourly,alerts"
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

            for (int i = 0; i < 7; i++) {  // Fetch 7 days correctly
                JSONObject dayData = jsonResponse.getJSONArray("daily").getJSONObject(i);
                double dayTemp = dayData.getJSONObject("temp").getDouble("day");
                String weatherDesc = dayData.getJSONArray("weather").getJSONObject(0).getString("description");

                JPanel dayPanel = new JPanel();
                dayPanel.setPreferredSize(new Dimension(100, 100));
                dayPanel.setBackground(new Color(135, 206, 250));
                dayPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                JLabel dayLabel = new JLabel("Day " + (i + 1));
                JLabel tempLabel = new JLabel(dayTemp + "°C");
                JLabel descLabel = new JLabel(weatherDesc);

                dayPanel.setLayout(new GridLayout(3, 1));
                dayPanel.add(dayLabel);
                dayPanel.add(tempLabel);
                dayPanel.add(descLabel);

                forecastPanel.add(dayPanel);
            }

            forecastPanel.revalidate();
            forecastPanel.repaint();
            frame.revalidate();
            frame.repaint();



        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching forecast data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new WeatherApp();
    }
}
