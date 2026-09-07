# Chapter 6: Spring Security Architecture & Filter Chain Mechanics

Spring Security provides authentication, authorization, and protection against common exploits (CSRF, CORS, session fixation) using a chain of Servlet Filters.

---

## 1. Servlet Filter Integration: `DelegatingFilterProxy` & `FilterChainProxy`

Spring Security hooks into the Servlet container (Tomcat) via a proxy filter bridge.

```
Servlet Container (Tomcat Filter Chain)
       |
       v
[ DelegatingFilterProxy ] (Servlet Filter registered in web container)
       |
       | Delegates execution to Spring-managed Bean ("springSecurityFilterChain")
       v
[ FilterChainProxy ] (Spring Security Master Filter)
       |
       v Matches URL Request Pattern
[ SecurityFilterChain ] (Ordered list of Security Filters)
       ├── 1. CorsFilter / CsrfFilter
       ├── 2. BearerTokenAuthenticationFilter (JWT)
       ├── 3. ExceptionTranslationFilter
       └── 4. AuthorizationFilter
       |
       v
[ DispatcherServlet ] (Spring MVC Controller)
```

---

## 2. The Core Authentication Pipeline

```
Client Request (Bearer JWT Token)
       |
       v
[ BearerTokenAuthenticationFilter ]
       |
       ├── Extracts Token, constructs unauthenticated JwtAuthenticationToken
       v
[ AuthenticationManager ] (ProviderManager)
       |
       ├── Delegates to matching AuthenticationProvider (JwtAuthenticationProvider)
       v
[ AuthenticationProvider ]
       |
       ├── Validates JWT signature, claims, and expiry
       ├── Loads GrantedAuthorities (e.g. ROLE_ADMIN)
       └── Returns fully authenticated Authentication object (isAuthenticated = true)
       |
       v
[ SecurityContextHolder ]
       └── Sets SecurityContext in ThreadLocal (SecurityContextHolder.getContext().setAuthentication(auth))
```

---

## 3. `SecurityContextHolder` & ThreadLocal Storage

By default, Spring Security stores the `Authentication` object in `SecurityContextHolder` using a Java `ThreadLocal`.

```java
// Retrieving active authenticated user anywhere in thread execution:
SecurityContext context = SecurityContextHolder.getContext();
Authentication authentication = context.getAuthentication();
String username = authentication.getName();
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
```

### 3.1 SecurityContext Storage Modes
* `MODE_THREADLOCAL` (Default): `SecurityContext` is isolated to the executing thread.
* `MODE_INHERITABLETHREADLOCAL`: Child threads spawned by the parent thread inherit the `SecurityContext`.
* `MODE_GLOBAL`: Shared across all threads (useful for standalone rich client applications).

---

## 4. Modern Stateless Security Configuration (Spring Security 6+)

Spring Security 6 enforces a component-based configuration style using `SecurityFilterChain` bean definitions instead of inheriting from legacy `WebSecurityConfigurerAdapter`.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Disabled for stateless JWT APIs
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

---

## 5. Staff Engineer Security Checklist
1. **Clear SecurityContext on Async Thread Handoffs:** When passing execution to `@Async` threads or custom thread pools, use `DelegatingSecurityContextExecutor` to propagate and clean up `SecurityContext` safely.
2. **Never Disable CSRF for Cookie-Based Sessions:** Only disable CSRF (`csrf.disable()`) if your API is completely stateless and relies exclusively on HTTP `Authorization: Bearer` headers.
3. **Use Fine-Grained Method Security:** Annotate service methods with `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("#userId == authentication.principal.subject")` for defense-in-depth authorization beyond URL routing.
