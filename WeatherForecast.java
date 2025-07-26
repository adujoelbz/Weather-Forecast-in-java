import java.util.*;
import java.text.DecimalFormat;

/**
 * Enhanced Weather Forecasting System using Runge-Kutta Method
 * Models atmospheric conditions with improved physical accuracy
 */
public class WeatherForecast {

    // Weather state variables with physical constraints
    static class WeatherState {
        double temperature;  // °C (realistic range -50 to 60)
        double pressure;     // hPa (870-1080)
        double humidity;     // % (0-100)
        double windSpeed;    // m/s (0-40)
        double windDirection; // degrees (0-360)
        
        public WeatherState(double temp, double press, double humid, double wind, double windDir) {
            this.temperature = temp;
            this.pressure = press;
            this.humidity = humid;
            this.windSpeed = wind;
            this.windDirection = windDir;
        }
        
        public WeatherState copy() {
            return new WeatherState(temperature, pressure, humidity, windSpeed, windDirection);
        }
        
        public void add(WeatherState other, double factor) {
            this.temperature += factor * other.temperature;
            this.pressure += factor * other.pressure;
            this.humidity += factor * other.humidity;
            this.windSpeed += factor * other.windSpeed;
            this.windDirection += factor * other.windDirection;
        }
    }
    
    // Enhanced atmospheric parameters
    private static final double SOLAR_CONSTANT = 0.1;
    private static final double COOLING_RATE = 0.05;
    private static final double PRESSURE_GRADIENT = 0.02;
    private static final double HUMIDITY_FACTOR = 0.03;
    private static final double WIND_FRICTION = 0.1;
    private static final double CORIOLIS_EFFECT = 0.001;
    private static final double LATENT_HEAT = 0.07;
    private static final double ADIABATIC_LAPSE = 0.0065;
    private static final double EARTH_RADIUS = 6371000;
    
    // Geographic parameters
    private static double latitude = 45.0;
    private static double elevation = 100.0;
    
    /**
     * Enhanced derivative calculations with improved physics
     */
    public static WeatherState calculateDerivatives(WeatherState state, double time) {
        WeatherState derivatives = new WeatherState(0, 0, 0, 0, 0);
        
        // 1. Temperature dynamics with elevation effects
        double solarHeating = SOLAR_CONSTANT * Math.sin(time * Math.PI / 12);
        double radiativeCooling = COOLING_RATE * (state.temperature - 15);
        double evaporativeCooling = 0.01 * state.humidity * (state.temperature > 20 ? 1 : 0);
        double elevationEffect = -ADIABATIC_LAPSE * elevation;
        derivatives.temperature = solarHeating - radiativeCooling - evaporativeCooling + elevationEffect;
        
        // 2. Pressure dynamics with altitude correction
        double pressureHeightFactor = Math.exp(-elevation/8500.0);
        derivatives.pressure = (-PRESSURE_GRADIENT * (state.temperature - 20) - 
                              0.01 * state.windSpeed * state.windSpeed) * pressureHeightFactor;
        
        // 3. Humidity dynamics with latent heat
        double maxHumidity = 100 - (state.temperature - 20) * 2;
        double evaporation = state.temperature > 25 ? 0.5 : 0.2;
        double condensation = state.humidity > maxHumidity ? 0.8 : 0;
        derivatives.humidity = (evaporation - condensation - 0.01 * state.windSpeed) * 
                             (1 + LATENT_HEAT * state.temperature/30.0);
        
        // 4. Wind dynamics with geostrophic balance
        double pressureForce = (1013 - state.pressure) * 0.1;
        double coriolisForce = CORIOLIS_EFFECT * state.windSpeed * Math.sin(Math.toRadians(latitude));
        double friction = WIND_FRICTION * state.windSpeed;
        double geostrophicWind = (1/(2*7.2921e-5*Math.sin(Math.toRadians(latitude)))) * 
                               (1013 - state.pressure)/100000.0;
        derivatives.windSpeed = 0.1 * (geostrophicWind - state.windSpeed) + pressureForce + coriolisForce - friction;
        
        // 5. Wind direction changes
        derivatives.windDirection = 0.3 * (270 - state.windDirection) + 
                                  2.0 * pressureForce + 
                                  0.5 * (Math.random() - 0.5);
        
        return derivatives;
    }
    
    /**
     * Fourth-order Runge-Kutta method implementation
     */
    public static WeatherState rungeKutta4(WeatherState currentState, double time, double stepSize) {
        WeatherState k1 = calculateDerivatives(currentState, time);
        
        WeatherState temp1 = currentState.copy();
        temp1.add(k1, stepSize / 2);
        WeatherState k2 = calculateDerivatives(temp1, time + stepSize / 2);
        
        WeatherState temp2 = currentState.copy();
        temp2.add(k2, stepSize / 2);
        WeatherState k3 = calculateDerivatives(temp2, time + stepSize / 2);
        
        WeatherState temp3 = currentState.copy();
        temp3.add(k3, stepSize);
        WeatherState k4 = calculateDerivatives(temp3, time + stepSize);
        
        WeatherState result = currentState.copy();
        result.add(k1, stepSize / 6);
        result.add(k2, stepSize / 3);
        result.add(k3, stepSize / 3);
        result.add(k4, stepSize / 6);
        
        // Apply physical constraints
        result.humidity = Math.max(0, Math.min(100, result.humidity));
        result.windSpeed = Math.max(0, Math.min(40, result.windSpeed));
        result.pressure = Math.max(870, Math.min(1080, result.pressure));
        result.windDirection = (result.windDirection % 360 + 360) % 360;
        
        return result;
    }
    
    /**
     * Generate weather forecast for specified duration
     */
    public static List<WeatherState> forecast(WeatherState initialState, double hours, double stepSize) {
        List<WeatherState> forecast = new ArrayList<>();
        WeatherState currentState = initialState.copy();
        double currentTime = 0;
        
        forecast.add(currentState.copy());
        
        while (currentTime < hours) {
            currentState = rungeKutta4(currentState, currentTime, stepSize);
            currentTime += stepSize;
            forecast.add(currentState.copy());
        }
        
        return forecast;
    }
    
    /**
     * Advanced weather classification
     */
    public static String classifyWeather(WeatherState state) {
        StringBuilder condition = new StringBuilder();
        
        if (state.temperature < 0 && state.humidity > 80) {
            condition.append("Snow ");
        } else if (state.temperature < 5 && state.humidity > 90) {
            condition.append("Freezing Fog ");
        }
        
        if (state.humidity > 85 && state.temperature > 25) {
            condition.append("Thunderstorm ");
        } else if (state.humidity > 80) {
            condition.append("Rain ");
        }
        
        if (state.windSpeed > 15) {
            condition.append("Gale ");
        } else if (state.windSpeed > 10) {
            condition.append("Breezy ");
        }
        
        if (condition.length() == 0) {
            if (state.temperature > 30) {
                return "Hot and Dry";
            }
            return "Clear Skies";
        }
        return condition.toString().trim();
    }
    
    /**
     * Calculate heat index (feels-like temperature)
     */
    private static double calculateHeatIndex(double temp, double humidity) {
        if (temp < 27) return temp;
        return -8.78469475556 + 1.61139411*temp + 2.33854883889*humidity +
               -0.14611605*temp*humidity + -0.012308094*Math.pow(temp,2) +
               -0.0164248277778*Math.pow(humidity,2) + 
               0.002211732*Math.pow(temp,2)*humidity +
               0.00072546*temp*Math.pow(humidity,2) +
               -0.000003582*Math.pow(temp,2)*Math.pow(humidity,2);
    }
    
    /**
     * Validate input parameters
     */
    private static void validateInput(double value, double min, double max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("%s must be between %.1f and %.1f", name, min, max));
        }
    }
    
    /**
     * Display formatted weather information with heat index
     */
    public static void displayWeather(double time, WeatherState state) {
        DecimalFormat df = new DecimalFormat("#.##");
        String condition = classifyWeather(state);
        double heatIndex = calculateHeatIndex(state.temperature, state.humidity);
        
        String windDirection;
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int dirIndex = (int)Math.round(((state.windDirection % 360) / 45)) % 8;
        windDirection = directions[dirIndex];
        
        System.out.printf("Time: %6.2f hrs | Temp: %6s°C (Feels: %6s°C) | Press: %7s hPa | " +
                         "Humid: %5s%% | Wind: %5s m/s %3s | Condition: %s%n",
                         time,
                         df.format(state.temperature),
                         df.format(heatIndex),
                         df.format(state.pressure),
                         df.format(state.humidity),
                         df.format(state.windSpeed),
                         windDirection,
                         condition);
    }
    
    /**
 * Plot simple ASCII graph of a weather variable
 */
private static void plotVariable(List<WeatherState> forecast, String variable, double stepSize) {
    System.out.println("\n" + variable + " over time:");
    
    // Find min and max values for scaling
    double minValue = Double.MAX_VALUE;
    double maxValue = Double.MIN_VALUE;
    
    for (int i = 0; i < forecast.size(); i += (int)(1.0/stepSize)) {
        if (i < forecast.size()) {
            double value = getVariableValue(forecast.get(i), variable);
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
        }
    }
    
    // Calculate scaling factor for the graph
    int maxWidth = 50; // Maximum width of the graph in characters
    double scale = maxWidth / (maxValue - minValue + 1);
    
    // Plot each data point
    for (int i = 0; i < forecast.size(); i += (int)(1.0/stepSize)) {
        if (i < forecast.size()) {
            double value = getVariableValue(forecast.get(i), variable);
            String unit = getVariableUnit(variable);
            
            // Scale the value for display
            int displayLength = (int)((value - minValue) * scale);
            displayLength = Math.max(0, Math.min(maxWidth, displayLength));
            
            System.out.printf("%4.1f %3s | %s%n", 
                value, unit,
                "=".repeat(displayLength));
        }
    }
}

// Helper method to get variable value
private static double getVariableValue(WeatherState state, String variable) {
    switch (variable) {
        case "Temperature": return state.temperature;
        case "Pressure": return state.pressure - 1000; // Center around 1000 hPa
        case "Humidity": return state.humidity;
        case "Wind Speed": return state.windSpeed;
        default: return 0;
    }
}

// Helper method to get variable unit
private static String getVariableUnit(String variable) {
    switch (variable) {
        case "Temperature": return "°C";
        case "Pressure": return "hPa";
        case "Humidity": return "%";
        case "Wind Speed": return "m/s";
        default: return "";
    }
}
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== ENHANCED WEATHER FORECASTING SYSTEM ===");
        System.out.println("Using 4th-order Runge-Kutta Method with Improved Atmospheric Physics\n");
        
        // Get geographic parameters
        System.out.print("Enter latitude (degrees, 0-90, default 45): ");
        String latInput = scanner.nextLine();
        if (!latInput.isEmpty()) {
            latitude = Double.parseDouble(latInput);
            validateInput(latitude, 0, 90, "Latitude");
        }
        
        System.out.print("Enter elevation (meters, default 100): ");
        String elevInput = scanner.nextLine();
        if (!elevInput.isEmpty()) {
            elevation = Double.parseDouble(elevInput);
            validateInput(elevation, -100, 9000, "Elevation");
        }
        
        // Get initial conditions
        System.out.print("Enter initial temperature (°C, default 22): ");
        String tempInput = scanner.nextLine();
        double initialTemp = tempInput.isEmpty() ? 22.0 : Double.parseDouble(tempInput);
        validateInput(initialTemp, -50, 60, "Temperature");
        
        System.out.print("Enter initial pressure (hPa, default 1013): ");
        String pressInput = scanner.nextLine();
        double initialPress = pressInput.isEmpty() ? 1013.0 : Double.parseDouble(pressInput);
        validateInput(initialPress, 870, 1080, "Pressure");
        
        System.out.print("Enter initial humidity (%, default 60): ");
        String humidInput = scanner.nextLine();
        double initialHumid = humidInput.isEmpty() ? 60.0 : Double.parseDouble(humidInput);
        validateInput(initialHumid, 0, 100, "Humidity");
        
        System.out.print("Enter initial wind speed (m/s, default 5): ");
        String windInput = scanner.nextLine();
        double initialWind = windInput.isEmpty() ? 5.0 : Double.parseDouble(windInput);
        validateInput(initialWind, 0, 40, "Wind speed");
        
        System.out.print("Enter initial wind direction (degrees, default 180): ");
        String windDirInput = scanner.nextLine();
        double initialWindDir = windDirInput.isEmpty() ? 180.0 : Double.parseDouble(windDirInput);
        
        System.out.print("Enter forecast duration (hours, default 24): ");
        String durationInput = scanner.nextLine();
        double duration = durationInput.isEmpty() ? 24.0 : Double.parseDouble(durationInput);
        
        // Create initial weather state
        WeatherState initialState = new WeatherState(initialTemp, initialPress, initialHumid, 
                                                   initialWind, initialWindDir);
        
        // Generate forecast using RK4 method
        double stepSize = 0.1; // 6-minute intervals
        List<WeatherState> forecastData = forecast(initialState, duration, stepSize);
        
        System.out.println("\n=== WEATHER FORECAST RESULTS ===");
        System.out.println("Using enhanced atmospheric modeling with geographic parameters:");
        System.out.printf("Latitude: %.1f°, Elevation: %.0f m\n\n", latitude, elevation);
        
        // Display results at hourly intervals
        for (int i = 0; i < forecastData.size(); i += (int)(1.0 / stepSize)) {
            if (i < forecastData.size()) {
                double time = i * stepSize;
                displayWeather(time, forecastData.get(i));
            }
        }
        
        // Summary statistics
        System.out.println("\n=== FORECAST SUMMARY ===");
        double avgTemp = forecastData.stream().mapToDouble(s -> s.temperature).average().orElse(0);
        double maxTemp = forecastData.stream().mapToDouble(s -> s.temperature).max().orElse(0);
        double minTemp = forecastData.stream().mapToDouble(s -> s.temperature).min().orElse(0);
        double avgHumidity = forecastData.stream().mapToDouble(s -> s.humidity).average().orElse(0);
        double maxWind = forecastData.stream().mapToDouble(s -> s.windSpeed).max().orElse(0);
        
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("Temperature - Avg: " + df.format(avgTemp) + "°C, " +
                          "Max: " + df.format(maxTemp) + "°C, " +
                          "Min: " + df.format(minTemp) + "°C");
        System.out.println("Average Humidity: " + df.format(avgHumidity) + "%");
        System.out.println("Maximum Wind Speed: " + df.format(maxWind) + " m/s");
        
        // Weather alerts
        System.out.println("\n=== WEATHER ALERTS ===");
        boolean hasAlerts = false;
        if (maxTemp > 35) {
            System.out.println("⚠  HEAT WARNING: Temperatures may exceed 35°C");
            hasAlerts = true;
        }
        if (maxWind > 20) {
            System.out.println("⚠  WIND WARNING: Strong winds expected (>" + df.format(maxWind) + " m/s)");
            hasAlerts = true;
        }
        if (avgHumidity > 85) {
            System.out.println("⚠  HUMIDITY WARNING: Fog or storm conditions possible");
            hasAlerts = true;
        }
        if (minTemp < 0) {
            System.out.println("⚠  FREEZE WARNING: Temperatures may drop below freezing");
            hasAlerts = true;
        }
        if (!hasAlerts) {
            System.out.println("✅ No severe weather warnings for the forecast period");
        }
        
        // Optional: Display simple graphs
        System.out.print("\nWould you like to view weather variable graphs? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            plotVariable(forecastData, "Temperature", stepSize);
            plotVariable(forecastData, "Pressure", stepSize);
            plotVariable(forecastData, "Humidity", stepSize);
            plotVariable(forecastData, "Wind Speed", stepSize);
        }
        
        scanner.close();
    }
}