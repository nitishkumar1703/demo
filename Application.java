import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import javax.persistence.*;
import java.util.*;

@SpringBootApplication
@EnableJpaRepositories
@EnableMethodSecurity
public class SpillGuardBackend {

    public static void main(String[] args) {
        SpringApplication.run(SpillGuardBackend.class, args);
    }

    // Entities
    @Entity
    static class User {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(unique = true) private String username;
        @Column(unique = true) private String email;
        private String phone;
        private String password;
        // Getters and setters
    }

    @Entity
    static class Sensor {
        @Id private String id;
        private double leakage;
        private int updateInterval = 5;
        // Getters and setters
    }

    // Repositories
    interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
        boolean existsByEmail(String email);
        boolean existsByUsername(String username);
    }

    interface SensorRepository extends JpaRepository<Sensor, String> {
        List<Sensor> findByIdContaining(String query);
    }

    // DTOs
    record LoginRequest(String email, String password) {}
    record SignupRequest(String username, String email, String phone, 
                        String password, String confirmPassword) {}

    // Controllers
    @RestController
    @RequestMapping("/api/auth")
    static class AuthController {
        private final UserRepository userRepo;
        private final PasswordEncoder encoder;

        public AuthController(UserRepository userRepo, PasswordEncoder encoder) {
            this.userRepo = userRepo;
            this.encoder = encoder;
        }

        @PostMapping("/signup")
        public ResponseEntity<?> register(@RequestBody SignupRequest req) {
            if (userRepo.existsByUsername(req.username())) 
                return ResponseEntity.badRequest().body("Username taken");
            if (userRepo.existsByEmail(req.email()))
                return ResponseEntity.badRequest().body("Email exists");
            if (!req.password().equals(req.confirmPassword()))
                return ResponseEntity.badRequest().body("Password mismatch");
            
            User user = new User();
            user.setUsername(req.username());
            user.setEmail(req.email());
            user.setPhone(req.phone());
            user.setPassword(encoder.encode(req.password()));
            userRepo.save(user);
            return ResponseEntity.ok("User created");
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest req) {
            Optional<User> user = userRepo.findByEmail(req.email());
            if (user.isEmpty() || !encoder.matches(req.password(), user.get().getPassword()))
                return ResponseEntity.status(401).body("Invalid credentials");
            return ResponseEntity.ok().header("Authorization", "BasicAuth").body("Logged in");
        }
    }

    @RestController
    @RequestMapping("/api/sensors")
    static class SensorController {
        private final SensorRepository sensorRepo;

        public SensorController(SensorRepository sensorRepo) {
            this.sensorRepo = sensorRepo;
        }

        @GetMapping
        public List<Sensor> getAll() {
            return sensorRepo.findAll();
        }

        @GetMapping("/search")
        public List<Sensor> search(@RequestParam String query) {
            return sensorRepo.findByIdContaining(query);
        }

        @PutMapping("/{id}/interval")
        public ResponseEntity<?> updateInterval(@PathVariable String id, @RequestParam int minutes) {
            Optional<Sensor> sensor = sensorRepo.findById(id);
            if (sensor.isEmpty()) return ResponseEntity.notFound().build();
            sensor.get().setUpdateInterval(minutes);
            sensorRepo.save(sensor.get());
            return ResponseEntity.ok("Interval updated");
        }
    }

    // Security Config
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic()
            .and().build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

/*
Add to application.properties:
spring.datasource.url=jdbc:h2:mem:spillguard
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update

Required dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- H2 Database
*/