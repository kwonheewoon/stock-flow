package io.khw.ranking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.blockhound.BlockHound;

@SpringBootApplication(scanBasePackages = {"io.khw.domain", "io.khw.common", "io.khw.ranking"})
@EnableR2dbcRepositories(basePackages = {"io.khw.domain.*.repository"})
@EntityScan(basePackages = {"io.khw.domain"})
@ConfigurationPropertiesScan(basePackages = "io.khw.common")
public class RankingModuleApplication {

    public static void main(String[] args) {
                BlockHound.builder()
                .allowBlockingCallsInside("java.io.RandomAccessFile", "readBytes")
                .install();
        SpringApplication.run(RankingModuleApplication.class, args);
    }

}
