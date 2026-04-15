package com.example.demo.Services;

import com.example.demo.Entities.SensorData;
import com.example.demo.Entities.Users;
import com.example.demo.Repositories.SensorDataRepository;
import com.example.demo.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DummySensorDataService {

    private final SensorDataRepository sensorRepo;
    private final UserRepository userRepo;
    private final Random random = new Random();

    // Run every 2 minutes (120000 ms) automatically
    @Scheduled(fixedRate = 120000)
    public void generateDataIfNoResponse() {
        List<Users> users = userRepo.findAll();
        // Threshold: 2 minutes without new data implies no response from actual sensors
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);

        for (Users user : users) {
            sensorRepo.findTopByUserIdOrderByIdDesc(user.getId()).ifPresentOrElse(lastReading -> {
                if (lastReading.getTimestamp().isBefore(threshold)) {
                    insertFallbackData(user);
                }
            }, () -> {
                // No reading exists at all, generate fresh ones
                insertFallbackData(user);
            });
        }
    }

    private void insertFallbackData(Users user) {
        List<String> sensorTypes = List.of("Temperature", "Humidity", "Soil Moisture");

        for (String type : sensorTypes) {
            SensorData data = new SensorData();
            data.setUser(user);
            data.setSensorType(type);
            data.setTimestamp(LocalDateTime.now());

            if (type.equals("Temperature")) {
                data.setValue(15 + random.nextDouble() * 15); // 15–30 °C
            } else if (type.equals("Humidity")) {
                data.setValue(Math.floor(40 + random.nextDouble() * 30)); // 40–70 %
            } else if (type.equals("Soil Moisture")) {
                data.setValue(Math.floor(20 + random.nextDouble() * 50)); // 20–70 %
            }

            sensorRepo.save(data);
        }
        System.out.println("Generated fallback dummy data for User ID: " + user.getId());
    }

    public void generateDummyData(Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> sensorTypes = List.of("Temperature", "Humidity", "Soil Moisture");

        for (String type : sensorTypes) {
            for (int i = 0; i < 20; i++) {
                SensorData data = new SensorData();
                data.setUser(user);
                data.setSensorType(type);

                // Assign random value
                if (type.equals("Temperature")) {
                    data.setValue(15 + random.nextDouble() * 15); // 15–30 °C
                } else if (type.equals("Humidity")) {
                    data.setValue(40 + random.nextDouble() * 30); // 40–70 %
                } else if (type.equals("Soil Moisture")) {
                    data.setValue(20 + random.nextDouble() * 50); // 20–70 %
                }

                // Random timestamp within last 60 days
                data.setTimestamp(LocalDateTime.now()
                        .minusDays(random.nextInt(60))
                        .minusHours(random.nextInt(24)));

                sensorRepo.save(data);
            }
        }
    }
}
