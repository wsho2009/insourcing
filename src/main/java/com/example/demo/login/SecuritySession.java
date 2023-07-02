package com.example.demo.login;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

//https://akira2kun.hatenablog.com/entry/2022/02/01/000000#:~:text=UserDetails%E3%82%AF%E3%83%A9%E3%82%B9%E3%81%AE%E3%82%AA%E3%83%96%E3%82%B8%E3%82%A7%E3%82%AF%E3%83%88%E3%81%AB,%E3%81%93%E3%81%A8%E3%81%A7%E5%AE%9F%E7%8F%BE%E3%81%A7%E3%81%8D%E3%81%BE%E3%81%99%E3%80%82
@Component
public class SecuritySession {

    public String getUsername() {
        // SecurityContextHolderから
        // org.springframework.security.core.Authenticationオブジェクトを取得
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null) {
            // AuthenticationオブジェクトからUserDetailsオブジェクトを取得
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // UserDetailsオブジェクトから、ユーザ名を取得
                return ( (UserDetails) principal ).getUsername();
            }
        }
        return null;
    }
    
    public String getName() {
        // SecurityContextHolderから
        // org.springframework.security.core.Authenticationオブジェクトを取得
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null) {
            // AuthenticationオブジェクトからUserDetailsオブジェクトを取得
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // UserDetailsオブジェクトから、ユーザ名を取得
                return ( (LoginUserDetails) principal ).getName();
            }
        }
        return null;
    }

    public String getCode() {
        // SecurityContextHolderから
        // org.springframework.security.core.Authenticationオブジェクトを取得
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null) {
            // AuthenticationオブジェクトからUserDetailsオブジェクトを取得
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // UserDetailsオブジェクトから、ユーザ名を取得
                return ( (LoginUserDetails) principal ).getCode();
            }
        }
        return null;
    }}