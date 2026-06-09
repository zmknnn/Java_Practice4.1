package com.example.app;

import annotations.GenerateJsonSerializer;
import annotations.JsonField;
import annotations.JsonIgnore;


@GenerateJsonSerializer
public class User {
    @JsonField(name = "user_name")
    private String name;

    @JsonField
    private int age;

    @JsonIgnore
    private String password;

    public User(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPassword() { return password; }
}