package org.example.stcapstonebackend;

import org.example.stcapstonebackend.common.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "riot.api.key=test-key"
})
class WebClientConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testWebClientBeansAreConfigured() {
        // Verify that WebClient beans are properly configured
        assertTrue(applicationContext.containsBean("riotKrWebClient"), 
            "riotKrWebClient bean should be present");
        assertTrue(applicationContext.containsBean("riotAsiaWebClient"), 
            "riotAsiaWebClient bean should be present");
        
        WebClient krWebClient = applicationContext.getBean("riotKrWebClient", WebClient.class);
        WebClient asiaWebClient = applicationContext.getBean("riotAsiaWebClient", WebClient.class);
        
        assertNotNull(krWebClient, "riotKrWebClient should not be null");
        assertNotNull(asiaWebClient, "riotAsiaWebClient should not be null");
    }

    @Test
    void testNoReactiveWebStackConfigured() {
        // Verify that reactive web stack is NOT configured
        // The application should use servlet-based web stack
        assertFalse(applicationContext.containsBean("webFluxConfiguration"),
            "WebFlux configuration should not be present");
    }
}
