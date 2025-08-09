package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.AuthLoginRequest;
import lk.acpt.smartbiz.dto.AuthRegisterRequest;
import lk.acpt.smartbiz.dto.AuthResponse;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.AuthService;
import lk.acpt.smartbiz.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String register(AuthRegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "OWNER");
        userRepo.save(user);
        return "User registered successfully";
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        // Authenticate (will throw AuthenticationException on failure)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Fetch user from DB to return role/email and include claims if needed
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Include extra claims (role & name) in the token payload if you'd like
        String token = jwtUtil.generateToken(request.getEmail(), user.getRole(), user.getName());

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}
