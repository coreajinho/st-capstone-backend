# Bean Conflict Fix - Spring Boot Startup Issue

## Problem
The application was failing to start with a bean definition conflict error:
```
The bean 'conversionServicePostProcessor', defined in class path resource 
[org/springframework/security/config/annotation/web/configuration/WebSecurityConfiguration.class], 
could not be registered. A bean with that name has already been defined in class path resource 
[org/springframework/security/config/annotation/web/reactive/WebFluxSecurityConfiguration.class] 
and overriding is disabled.
```

## Root Cause
The application had both:
- `spring-boot-starter-web` (servlet-based web stack)
- `spring-boot-starter-webflux` (reactive web stack)

Having both starters caused Spring Security to load both servlet and reactive configurations, resulting in duplicate bean definitions.

## Solution
Since the application uses:
- **Servlet-based controllers** (`@RestController` with `ResponseEntity`)
- **WebClient** for external API calls (Riot API)
- **Servlet architecture** (not fully reactive)

We kept the servlet stack and removed the full reactive starter:

### Changes in `build.gradle`:
**Before:**
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

**After:**
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'

//WebFlux for WebClient (HTTP client only, not reactive web stack)
implementation 'org.springframework:spring-webflux'
implementation 'io.projectreactor.netty:reactor-netty'
```

## Result
✅ Application uses **Tomcat** (servlet web server)  
✅ **WebClient** functionality is preserved for external API calls  
✅ **No bean conflicts** between servlet and reactive security configurations  
✅ All existing functionality works as expected  

## Testing
Added comprehensive tests to verify:
1. `WebClientConfigTest` - Confirms WebClient beans are configured
2. `NoBeanConflictTest` - Verifies no bean conflicts and servlet stack usage

## Recommendation
This solution is appropriate when:
- You need servlet-based controllers (traditional REST APIs)
- You want to use WebClient for reactive HTTP calls to external services
- You don't need a fully reactive application stack

If you need a **fully reactive application**, consider:
- Removing `spring-boot-starter-web`
- Keeping `spring-boot-starter-webflux`
- Converting all controllers to use reactive types (`Mono`, `Flux`)
- Using reactive repositories
