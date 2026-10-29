package com.opsmind.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.opsmind.ai.repository")
public class HibernateConfig {
    // Explicit configuration ensuring Hibernate 6 JPA transaction management & repositories are active
}
