package com.vbforge.jwtsecurity.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private Integer id;
    private String username;
    private String password;
    private String role;



}
