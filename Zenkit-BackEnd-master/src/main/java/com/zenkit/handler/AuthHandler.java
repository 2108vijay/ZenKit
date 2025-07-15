package com.zenkit.handler;

import com.zenkit.util.EmailUtil;
import com.zenkit.util.JwtUtil;
import com.zenkit.util.PasswordUtil;
import com.zenkit.util.TokenStore;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class AuthHandler {
    private final MongoClient mongoClient;
    private final EmailUtil emailUtil;

    public AuthHandler(MongoClient mongoClient, Vertx vertx) {
        this.mongoClient = mongoClient;
        this.emailUtil = new EmailUtil(vertx);
    }

    // Handle user registration
    public void handleRegister(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String fullName = body.getString("fullName");
        String email = body.getString("email");
        String password = body.getString("password");

        if (fullName == null || email == null || password == null) {
            ctx.response().setStatusCode(400).end("Missing fields");
            return;
        }

        mongoClient.findOne("users", new JsonObject().put("email", email), null)
                .onSuccess(existingUser -> {
                    if (existingUser != null) {
                        ctx.response().setStatusCode(409).end("Email already registered");
                    } else {
                        JsonObject userDoc = new JsonObject()
                                .put("fullName", fullName)
                                .put("email", email)
                                .put("password", PasswordUtil.hashPassword(password));

                        mongoClient.insert("users", userDoc)
                                .onSuccess(id -> ctx.response().setStatusCode(201).end("User registered successfully"))
                                .onFailure(err -> ctx.response().setStatusCode(500).end("Failed to register user"));
                    }
                })
                .onFailure(err -> ctx.response().setStatusCode(500).end("Database error"));
    }

    // Handle login
    public void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String password = body.getString("password");

        if (email == null || password == null) {
            ctx.response().setStatusCode(400).end("Missing credentials");
            return;
        }

        mongoClient.findOne("users", new JsonObject().put("email", email), null)
                .onSuccess(user -> {
                    if (user == null) {
                        ctx.response().setStatusCode(404).end("User not found");
                    } else {
                        String storedHash = user.getString("password");
                        if (!PasswordUtil.hashPassword(password).equals(storedHash)) {
                            ctx.response().setStatusCode(401).end("Invalid password");
                        } else {
                            String token = JwtUtil.generateToken(user.getString("_id"), email);
                            JsonObject response = new JsonObject()
                                    .put("token", token)
                                    .put("fullName", user.getString("fullName"));
                            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.encode());
                        }
                    }
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end("Login error");
                });
    }

    // Handle forgot password (send verification code)
    public void handleForgotPassword(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");

        if (email == null) {
            ctx.response().setStatusCode(400).end("Email required");
            return;
        }

        mongoClient.findOne("users", new JsonObject().put("email", email), null)
                .onSuccess(user -> {
                    if (user == null) {
                        ctx.response().setStatusCode(404).end("User not found");
                    } else {
                        String code = String.valueOf((int)(100000 + Math.random() * 900000));
                        TokenStore.storeToken(email, code);
                        emailUtil.sendResetCode(email, code);
                        ctx.response().setStatusCode(200).end("Reset code sent to email");
                    }
                })
                .onFailure(err -> ctx.response().setStatusCode(500).end("Error sending code"));
    }
    public void handleChangePassword(RoutingContext ctx) {
        String userId = ctx.get("userId");
        JsonObject body = ctx.body().asJsonObject();

        String currentPassword = body.getString("currentPassword");
        String newPassword = body.getString("newPassword");

        if (currentPassword == null || newPassword == null) {
            ctx.response().setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("message", "Missing fields").encode());
            return;
        }

        mongoClient.findOne("users", new JsonObject().put("_id", userId), null)
                .onSuccess(user -> {
                    if (user == null) {
                        ctx.response().setStatusCode(404)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject().put("message", "User not found").encode());
                        return;
                    }

                    String storedHash = user.getString("password");

                    if (!PasswordUtil.hashPassword(currentPassword).equals(storedHash)) {
                        ctx.response().setStatusCode(401)
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonObject().put("message", "Incorrect current password").encode());
                        return;
                    }

                    String newHashedPassword = PasswordUtil.hashPassword(newPassword);
                    JsonObject update = new JsonObject().put("$set", new JsonObject().put("password", newHashedPassword));

                    mongoClient.updateCollection("users", new JsonObject().put("_id", userId), update)
                            .onSuccess(v -> ctx.response().setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("message", "Password changed successfully").encode()))
                            .onFailure(err -> ctx.response().setStatusCode(500)
                                    .putHeader("Content-Type", "application/json")
                                    .end(new JsonObject().put("message", "Failed to update password").encode()));
                })
                .onFailure(err -> ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Database error").encode()));
    }
    // Handle verify code (new method added)
    public void handleVerifyCode(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String code = body.getString("code");

        if (email == null || code == null) {
            ctx.response().setStatusCode(400).end("Missing fields");
            return;
        }

        if (TokenStore.verifyToken(email, code)) {
            ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("valid", true).encode());
        } else {
            ctx.response().setStatusCode(401).end("Invalid or expired code");
        }
    }

    // Handle reset password
    public void handleResetPassword(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String token = body.getString("token");
        String newPassword = body.getString("newPassword");

        if (email == null || token == null || newPassword == null) {
            ctx.response().setStatusCode(400).end("Missing fields");
            return;
        }

        if (!TokenStore.verifyToken(email, token)) {
            ctx.response()
                    .setStatusCode(401).end("Invalid token");
            return;
        }

        String hashed = PasswordUtil.hashPassword(newPassword);
        JsonObject update = new JsonObject().put("$set", new JsonObject().put("password", hashed));

        mongoClient.updateCollection("users", new JsonObject().put("email", email), update)
                .onSuccess(res -> {
                    TokenStore.removeToken(email);
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .setStatusCode(200)
                            .end(new JsonObject().put("message", "Password updated").encode());

                })
                .onFailure(err -> ctx.response().setStatusCode(500).end("Failed to update password"));
    }
}
