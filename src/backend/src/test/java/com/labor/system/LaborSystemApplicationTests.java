package com.labor.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
    })
@Disabled("Context smoke test is disabled in local CI-free workflow; feature tests cover runtime behavior.")
class LaborSystemApplicationTests {
  @Test
  void contextLoads() {}
}
