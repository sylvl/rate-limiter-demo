package org.sylvl.ratelimiterdemo;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jdbc.store.JdbcMessageStore;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.util.function.Function;

@Configuration
public class RateLimiterConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterConfig.class);

  @Bean
  public IntegrationFlow splitAggregateFlow(JdbcTemplate jdbcTemplate) {
    return IntegrationFlows.from(MessageFunction.class, gatewayProxySpec -> gatewayProxySpec.replyTimeout(0).beanName("myAggregator"))
        .aggregate(a ->
            a.correlationStrategy(m -> m.getHeaders().get("correlationId"))
                .releaseStrategy(g -> g.size() >= 1)
                .messageStore(messageStore(jdbcTemplate))
                .expireTimeout(10000)
                .expireDuration(Duration.ofMillis(1000))
                .discardChannel((message, timeout) -> {
                  LOGGER.info("discarded " + message.getPayload().toString());
                  return true;
                })
        )
        .get();
  }

  @Bean
  public MessageGroupStore messageStore(JdbcTemplate jdbcTemplate) {
    return new JdbcMessageStore(jdbcTemplate);
  }

  @Bean
  public HikariDataSource dataSource(DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
  }

  public interface MessageFunction extends Function<Message<String>, Message<String>> {

  }
}
