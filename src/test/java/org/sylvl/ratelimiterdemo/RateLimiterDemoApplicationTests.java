package org.sylvl.ratelimiterdemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.PreDestroy;

@Testcontainers
@SpringBootTest
class RateLimiterDemoApplicationTests {
	private ApplicationContextRunner applicationContextRunner;

  @Container
  public RabbitMQContainer container = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.9"))
      .withExposedPorts(5672);

  @BeforeEach
  void setup() {
    applicationContextRunner = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class)
        .withPropertyValues(
            "spring.cloud.stream.binders.test.type=rabbit",
            "spring.cloud.stream.binders.test.environment.spring.rabbitmq.host=" + container.getHost(),
            "spring.cloud.stream.binders.test.environment.spring.rabbitmq.port=" + container.getAmqpPort()
            "spring.cloud.stream.binders.test.environment.spring.rabbitmq.username=" + container.getAdminUsername(),
            "spring.cloud.stream.binders.test.environment.spring.rabbitmq.password=" + container.getAdminPassword(),
            "spring.cloud.stream.binders.test.environment.spring.rabbitmq.virtual-host=/",
            "spring.cloud.stream.bindings.functionRouter-in-0.binder=test",
            "spring.cloud.stream.bindings.functionRouter-in-0.destination=logs",
            "spring.cloud.stream.bindings.functionRouter-in-0.group=default",
            "spring.cloud.stream.rabbit.bindings.functionRouter-in-0.consumer.auto-bind-dlq=true",
            "spring.cloud.stream.rabbit.bindings.functionRouter-in-0.consumer.dlqDeadLetterExchange=DLX",
            "spring.cloud.stream.bindings.functionRouter-out-0.binder=test",
            "spring.cloud.stream.bindings.functionRouter-out-0.destination=aggregated",
            "spring.cloud.stream.bindings.functionRouter-out-0.producer.required-groups=default",
            "spring.cloud.stream.rabbit.bindings.functionRouter-out-0.producer.auto-bind-dlq=true",
            "spring.cloud.stream.rabbit.bindings.functionRouter-out-0.producer.dlqDeadLetterExchange=DLX");
  }

  @Test
  void contextLoads() {
  }

  @SpringBootApplication
  static class TestConfig {

    @PreDestroy
    public void destroy() {
    }
  }
}
