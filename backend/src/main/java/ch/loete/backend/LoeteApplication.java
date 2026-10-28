package ch.loete.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoeteApplication {

  public static void main(String[] args) {
    SpringApplication.run(LoeteApplication.class, args);
  }
}
