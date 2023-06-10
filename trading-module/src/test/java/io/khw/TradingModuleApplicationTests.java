package io.khw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest(classes = TradingModuleApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@WebAppConfiguration()
class TradingModuleApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("dfsdf");
    }

}
