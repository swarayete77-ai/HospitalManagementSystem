package com.hospital.hospitalapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody Map<String, String> body,
                                   jakarta.servlet.http.HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(403).body("Admin token required to register users");
    }

    String token = authHeader.substring(7);
    if (!jwtUtil.isTokenValid(token) || !jwtUtil.extractRole(token).equals("ADMIN")) {
        return ResponseEntity.status(403).body("Only admins can register new users");
    }

    String username = body.get("username");
    String password = body.get("password");
    String role = body.get("role").toUpperCase();

    if (userRepository.findByUsername(username).isPresent()) {
        return ResponseEntity.badRequest().body("Username already exists");
    }

    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setRole(Role.valueOf(role));

    userRepository.save(user);
    return ResponseEntity.ok("User registered successfully");
}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(username, userOpt.get().getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "role", userOpt.get().getRole().name()));
    }
}