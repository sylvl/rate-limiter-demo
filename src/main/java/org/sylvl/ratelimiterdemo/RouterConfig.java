package org.sylvl.ratelimiterdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class RouterConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterConfig.class);

  @Bean
  public Router router() {
    return new Router();
  }

  @Bean
  public Consumer<String> rejected() {
    return s -> LOGGER.info("rejected : " + s);
  }

  private static class Router {
    public String route(String key) {
      if (key.contains("foo")) {
        return "myAggregator";
      } else {
        return "rejected";
      }
    }
  }
}