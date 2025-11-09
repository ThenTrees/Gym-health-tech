package com.thentrees.gymhealthtech.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

  @Bean
  public FirebaseAuth firebaseAuth() throws IOException {
    GoogleCredentials credentials = GoogleCredentials.fromStream(
      new ClassPathResource("serviceAccountKey.json").getInputStream()
    );

    FirebaseOptions options = FirebaseOptions.builder()
      .setCredentials(credentials)
      .setProjectId("gym-mate-8addf")
      .build();

    // Initialize only if not already done
    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseApp.initializeApp(options);
    }

    return FirebaseAuth.getInstance();
  }
}
