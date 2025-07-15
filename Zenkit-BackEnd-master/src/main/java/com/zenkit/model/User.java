package com.zenkit.model;

import io.vertx.core.json.JsonObject;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String password;

    public User() {}

    public User(JsonObject json) {
        this.id = json.getString("_id");
        this.fullName = json.getString("fullName");
        this.email = json.getString("email");
        this.password = json.getString("password");
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("fullName", fullName)
                .put("email", email)
                .put("password", password);
    }

    // Getters and Setters (optional)
}
