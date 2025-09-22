package lk.acpt.smartbiz.config;

import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run first
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@smartbiz.com";
        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("AdminPass123!")); // change in prod
            admin.setRole("ADMIN");
            userRepo.save(admin);
            System.out.println("✅ Default admin user created:");
            System.out.println("   Email: " + adminEmail);
            System.out.println("   Password: AdminPass123!");
            System.out.println("   ⚠️  Please change the default password in production!");
        } else {
            System.out.println("ℹ️  Admin user already exists, skipping creation");
        }
    }
}