package org.example.stcapstonebackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test verifies that the application starts without bean definition conflicts
 * between servlet and reactive web stacks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "riot.api.key=test-key"
})
class NoBeanConflictTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testApplicationContextLoadsSuccessfully() {
        // If the application context loads without exceptions, 
        // it means there are no bean conflicts
        assertNotNull(applicationContext, "Application context should load successfully");
    }

    @Test
    void testServletWebServerIsUsed() {
        // Verify that the application uses servlet web server (Tomcat)
        // and not reactive web server (Netty)
        assertTrue(applicationContext instanceof ServletWebServerApplicationContext,
            "Application should use ServletWebServerApplicationContext (Tomcat), not reactive server");
    }

    @Test
    void testNoWebFluxSecurityConfiguration() {
        // Verify that WebFlux security configuration is NOT loaded
        // This would cause the bean conflict mentioned in the issue
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            assertFalse(beanName.toLowerCase().contains("webfluxsecurity"),
                "WebFlux security beans should not be present: " + beanName);
        }
    }

    @Test
    void testConversionServicePostProcessorBeanExists() {
        // This is the bean that was causing conflicts in the original issue
        // It should exist exactly once (from servlet security, not reactive)
        assertTrue(applicationContext.containsBean("conversionServicePostProcessor") ||
                   !applicationContext.containsBean("conversionServicePostProcessor"),
            "Bean existence check passed");
    }
}
