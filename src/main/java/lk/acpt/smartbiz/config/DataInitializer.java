package lk.acpt.smartbiz.config;

import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@smartbiz.com";
        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("AdminPass123!")); // change in prod
            admin.setRole("ADMIN");
            userRepo.save(admin);
            System.out.println("Seeded admin user: " + adminEmail + " / AdminPass123!");
        }
    }
}
