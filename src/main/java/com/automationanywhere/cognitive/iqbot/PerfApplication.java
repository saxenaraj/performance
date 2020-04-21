package com.automationanywhere.cognitive.iqbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.automationanywhere.cognitive.iqbot.parser",
    "com.automationanywhere.cognitive.iqbot.processor"})
public class PerfApplication {

  public static void main(final String[] args) {
    SpringApplication.run(PerfApplication.class, args);
  }
}
