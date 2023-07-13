package com.example.demo.login;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//https://www.docswell.com/s/MasatoshiTada/KGVY9K-spring-security-intro#p34
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(login -> login           // フォーム認証の設定記述開始
                .loginProcessingUrl("/login")   // ユーザー、パスワードの送信先URL
                .loginPage("/login")            // ログイン画面のURL
                .defaultSuccessUrl("/upload")   // ログイン成功後のリダイレクト先URL
                .failureUrl("/login?error")     // ログイン失敗後のリダイレクト先URL
                .permitAll()                    // ログイン画面は、未ログインでもアクセス可能
        ).logout(logout -> logout
                .logoutSuccessUrl("/")
        ).authorizeHttpRequests(authz -> authz
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/").permitAll()	//ログインなしでもアクセス可能URL
                .requestMatchers("/fax").permitAll()
                .requestMatchers("/fax/delete").permitAll()
                .requestMatchers("/ocr/list").permitAll()
                .requestMatchers("/ocr/result").permitAll()
                .requestMatchers("/daicho").permitAll()
                .requestMatchers("/api/daicho").permitAll()
                .requestMatchers("/general").hasRole("GENERAL")
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()	//他のURLはログイン後のみアクセス可能
        );
        //https://qiita.com/Oki0837/items/89d424d1606168f218c1
        http.csrf().disable();	//CSRF対策を有効にしない
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder();
    	//https://qiita.com/teradatk/items/1e09f0ed4a29d2699504
        // テストのためパスワードの暗号化はしない
        return NoOpPasswordEncoder.getInstance();        
    }
}
