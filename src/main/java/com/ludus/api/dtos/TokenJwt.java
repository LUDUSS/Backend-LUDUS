package com.ludus.api.dtos;

public class TokenJwt {
    private String token;

    public TokenJwt(String token) {
        this.token = token;
    }

    public TokenJwt() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}