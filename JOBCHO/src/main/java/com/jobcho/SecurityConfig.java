package com.jobcho;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	    .authorizeHttpRequests((authorize) -> authorize
	            .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
	            .requestMatchers("/user/login", "/user/signup").permitAll()
	            .anyRequest().authenticated())
	    .formLogin((formLogin) -> formLogin
	            .usernameParameter("user_email")
	            .passwordParameter("user_password") 
	            .loginPage("/user/login")
	            .defaultSuccessUrl("/"))
	    .logout((logout) -> logout
	            .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
	            .logoutSuccessUrl("/")
	            .invalidateHttpSession(true))
	    .rememberMe((rememberMe) -> rememberMe
                .key("my-remember-me-key") 
                .rememberMeParameter("remember-me") 
                .tokenValiditySeconds(60 * 60 * 24 * 1)
        );
	    
	    return http.build();
	}


	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	

}
