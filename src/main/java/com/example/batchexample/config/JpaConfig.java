package com.example.batchexample.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@EnableJpaRepositories(
        basePackages = "com.example.batchexample",
        entityManagerFactoryRef = "joyEntityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@Configuration
@RequiredArgsConstructor
public class JpaConfig {


    @Bean("joyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean joyEntityManagerFactory(
            @Qualifier("joyDataSource") DataSource joyDataSource
    ) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        Properties properties = new Properties();
        properties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        factory.setPackagesToScan("com.example.batchexample");
        factory.setDataSource(joyDataSource);
        factory.setJpaProperties(properties);
        factory.setPersistenceUnitName("joyEntityManager");
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);
        jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        factory.setJpaVendorAdapter(jpaVendorAdapter);

        return factory;
    }

    @Bean("joyDataSource")
    @ConfigurationProperties("datasources.joy.datasource.hikari")
    DataSource joyDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean joyEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(joyEntityManagerFactory.getObject()));
    }


}
