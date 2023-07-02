package com.example.demo.login;

import java.util.List;

public record LoginUser(String email, String name, String password, String code, List<String> roleList) {
}
