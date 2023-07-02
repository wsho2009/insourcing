package com.example.demo.login;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginUserRepository {

    private static final String SQL_FIND_BY_EMAIL = """
            SELECT
              email,
              user_name,
              password, 
              code 
            FROM user_mst
            WHERE email = :email
            """;

    private static final ResultSetExtractor<LoginUser> LOGIN_USER_EXTRACTOR = (rs) -> {
        String email = null;
        String userName = null;
        String password = null;
        String code = null;
        List<String> roleList = new ArrayList<>();
        while (rs.next()) {
            if (email == null) {
                email = rs.getString("email");
                userName = rs.getString("user_name");
                password = rs.getString("password");
                code = rs.getString("code");
            }
            //roleList.add(rs.getString("role_name"));	//いったんコメントアウト
        }
        if (email == null) {
            return null;
        }
        return new LoginUser(email, userName, password, code, roleList);
    };

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public LoginUserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<LoginUser> findByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        LoginUser loginUser = namedParameterJdbcTemplate.query(SQL_FIND_BY_EMAIL, params, LOGIN_USER_EXTRACTOR);
        return Optional.ofNullable(loginUser);
    }
}
