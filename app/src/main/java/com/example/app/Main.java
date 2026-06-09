package com.example.app;

public class Main {
    public static void main(String[] args) throws Exception {
        User user = new User("Nonna", 19, "password");

        System.out.println("\n=== RUNTIME JSON ===");
        System.out.println(RuntimeJsonSerializer.toJson(user));

        System.out.println("\n=== COMPILE-TIME GENERATED JSON ===");
       // System.out.println(UserSerializer.toJson(user));
    }
}