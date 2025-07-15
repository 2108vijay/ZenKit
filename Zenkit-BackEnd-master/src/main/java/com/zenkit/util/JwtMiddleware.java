package com.zenkit.util;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.jsonwebtoken.Claims;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;

public class JwtMiddleware {
    public static Handler<RoutingContext> handle() {
        return ctx -> {
            String authHeader = ctx.request().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.response().setStatusCode(401).end("Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7); // Strip "Bearer "
            try {
                Claims claims = JwtUtil.verifyToken(token);
                String userId = claims.getSubject();
                String email = (String) claims.get("email");

                JsonObject principal = new JsonObject()
                        .put("userId", userId)
                        .put("email", email);

                ctx.setUser(User.create(principal));

                ctx.next(); // Proceed to the next handler
            } catch (Exception e) {
                ctx.response().setStatusCode(401).end("Invalid or expired token");
            }
        };
    }
}
