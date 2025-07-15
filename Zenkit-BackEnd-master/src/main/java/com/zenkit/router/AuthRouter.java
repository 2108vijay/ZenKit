package com.zenkit.router;

import com.zenkit.handler.AuthHandler;
import com.zenkit.middleware.AuthMiddleware;
import io.vertx.ext.web.Router;

public class AuthRouter {
    public static void setup(Router router, AuthHandler authHandler, AuthMiddleware authMiddleware) {
        router.post("/api/auth/register").handler(authHandler::handleRegister);
        router.post("/api/auth/login").handler(authHandler::handleLogin);router.post("/api/auth/change-password")
                .handler(authMiddleware::handle) // <== Middleware applied here
                .handler(authHandler::handleChangePassword);
        router.post("/api/auth/forgot-password").handler(authHandler::handleForgotPassword);
        router.post("/api/auth/reset-password").handler(authHandler::handleResetPassword);
        router.post("/api/auth/verify-code").handler(authHandler::handleVerifyCode);



    }
}
