package com.lostfound.config;

import com.lostfound.entity.Role;
import com.lostfound.entity.User;
import com.lostfound.repository.RoleRepository;
import com.lostfound.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo-users.enabled:false}")
    private boolean demoUsersEnabled;

    @Value("${app.admin.email:admin@lostfound.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.name:System Admin}")
    private String adminName;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_USER");
            return roleRepository.save(r);
        });

        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_ADMIN");
            return roleRepository.save(r);
        });

        createOrUpdateUser(adminEmail, adminName, adminUsername, adminPassword, adminRole, "admin");

        if (demoUsersEnabled) {
            log.info("DEMO MODE ENABLED - Creating demo users for testing");
            createOrUpdateUser("tester@example.com", "Test User", "tester", "pass123", userRole, "demo");
            createOrUpdateUser("tester2@example.com", "Test User 2", "tester2", "pass123", userRole, "demo");
        } else {
            log.info("Production mode - Demo users disabled for security");
        }
    }

    private void createOrUpdateUser(String email, String name, String username, String password, Role role, String userType) {
        User user = userRepository.findByEmail(email).orElse(null);
        boolean needsSave = false;
        
        if (user == null) {
            User existingByUsername = userRepository.findByUsername(username).orElse(null);
            if (existingByUsername != null) {
                log.warn("User with username '{}' already exists with email '{}'. Skipping creation.", username, existingByUsername.getEmail());
                return;
            }
            
            user = new User();
            user.setName(name);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(Collections.singleton(role));
            needsSave = true;
            log.info("Created {} user: {}", userType, email);
        } else {
            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
                needsSave = true;
                log.info("Updated {} user name: {} -> {}", userType, user.getName(), name);
            }
            
            if (username != null && !username.equals(user.getUsername())) {
                User existingByUsername = userRepository.findByUsername(username).orElse(null);
                if (existingByUsername != null && !existingByUsername.getId().equals(user.getId())) {
                    log.warn("Cannot update username to '{}' - already exists for another user. Keeping existing username: {}", username, user.getUsername());
                } else {
                    user.setUsername(username);
                    needsSave = true;
                    log.info("Updated {} user username: {}", userType, username);
                }
            }
            
            if (!passwordEncoder.matches(password, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(password));
                needsSave = true;
                if ("admin".equals(userType)) {
                    log.info("Updated admin user password: {}", email);
                }
            }
            if (!user.getRoles().contains(role)) {
                user.setRoles(Collections.singleton(role));
                needsSave = true;
            }
        }
        
        if (needsSave) {
            userRepository.save(user);
        }
    }
}


