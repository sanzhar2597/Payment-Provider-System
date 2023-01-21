package kz.duff.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${ib.auth.login}")
    private String ibLogin;

    @Value("${ib.auth.password}")
    private String ibPassword;

    @Value("${tnt.auth.login}")
    private String tntLogin;

    @Value("${tnt.auth.password}")
    private String tntPassword;

    @Value("${ibcnp.auth.login}")
    private String ibCnpLogin;

    @Value("${ibcnp.auth.password}")
    private String ibCnpPassword;

    @Value("${ufo.auth.login}")
    private String ufoLogin;

    @Value("${ufo.auth.password}")
    private String ufoPassword;

    @Value("${admin.auth.login}")
    private String adminLogin;

    @Value("${admin.auth.password}")
    private String adminPassword;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();

        auth
                .inMemoryAuthentication()
                .withUser(adminLogin)
                .password(encoder.encode(adminPassword))
                .roles("ADMIN");
        auth
                .inMemoryAuthentication()
                .withUser(ibLogin)
                .password(encoder.encode(ibPassword))
                .roles("USER");
        auth
                .inMemoryAuthentication()
                .withUser(ibCnpLogin)
                .password(encoder.encode(ibCnpPassword))
                .roles("USER");
        auth
                .inMemoryAuthentication()
                .withUser(tntLogin)
                .password(encoder.encode(tntPassword))
                .roles("USER");
        auth
                .inMemoryAuthentication()
                .withUser(ufoLogin)
                .password(encoder.encode(ufoPassword))
                .roles("USER");
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Disable CSRF
        http.csrf().disable().formLogin().disable()
                // any authenticated user can perform all other operations
                .authorizeRequests().anyRequest().hasAnyRole("ADMIN","USER").and().httpBasic()
                // We don't need sessions to be created.
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }

}
