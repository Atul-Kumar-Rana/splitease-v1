package com.example.splitwise;

import com.example.splitwise.model.User;
import com.example.splitwise.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserRepo userRepo;

    public CustomOAuth2UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("CustomOAuth2UserService.loadUser() called");

        // call parent to fetch attributes from provider
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attrs = oauthUser.getAttributes();
        log.info("OAuth attributes: {}", attrs);

        String email = (String) attrs.get("email");
        String name = (String) attrs.getOrDefault("name", attrs.get("given_name"));

        if (email == null || email.isBlank()) {
            log.warn("OAuth provider did not return an email. attrs={}", attrs);
            // optionally throw an exception if email is required
            return oauthUser;
        }

        // Find or create your local user
        Optional<User> existing = userRepo.findByEmail(email);
        User u;
        if (existing.isPresent()) {
            u = existing.get();
            u.setUsername(name != null ? name : u.getUsername());
            userRepo.save(u);
            log.info("Updated existing user id={} email={}", u.getId(), u.getEmail());
        } else {
            u = new User();
            u.setUsername(name != null ? name : email.split("@")[0]);
            u.setEmail(email);
            userRepo.save(u);
            log.info("Created new user id={} email={}", u.getId(), u.getEmail());
        }

        return oauthUser;
    }
}
