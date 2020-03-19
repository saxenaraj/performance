package com.automationanywhere.cognitive.iqbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@SpringBootConfiguration
@EnableAutoConfiguration
public class PerfApplication {
  public static void main(final String[] args) {
    SpringApplication.run(PerfApplication.class, args);
  }
}
