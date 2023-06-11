package io.khw.config;

import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@TestConfiguration
@AutoConfigureDataR2dbc
//@ComponentScan(basePackages = {"io.khw.domain.*.repository"})
@EnableR2dbcRepositories(basePackages = {"io.khw.domain.*.repository"})
public class TestConfig {
}
