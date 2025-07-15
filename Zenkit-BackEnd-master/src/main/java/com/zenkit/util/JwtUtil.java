package com.zenkit.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
//import io.vertx.ext.auth.Authorization;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.auth.authorization.Authorization;


import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "zenkittoolkitjwtsecrethere1234567890";
    private static final long EXPIRATION_MS = 15 * 60 * 1000;

    public static String generateToken(String userId, String email) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Claims verifyToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public static void extractUser(RoutingContext ctx) {
        String authHeader = ctx.request().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.response().setStatusCode(401).end("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = verifyToken(token);

            JsonObject principal = new JsonObject()
                    .put("userId", claims.getSubject())
                    .put("email", claims.get("email"));

            ctx.setUser(new User() {
                @Override
                public JsonObject principal() {
                    return principal;
                }

                @Override
                public void setAuthProvider(io.vertx.ext.auth.AuthProvider authProvider) {}

                @Override
                public <T> T get(String key) {
                    return null;
                }

                @Override
                public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
                    resultHandler.handle(Future.succeededFuture(true));
                    return this;
                }

                @Override
                public User isAuthorized(Authorization authorization, Handler<AsyncResult<Boolean>> resultHandler) {
                    resultHandler.handle(Future.succeededFuture(true));
                    return this;
                }

                @Override
                public JsonObject attributes() {
                    return new JsonObject();
                }

                @Override
                public User merge(User other) {
                    return this;
                }

                @Override
                public User clearCache() {
                    return this;
                }
            });

            ctx.next();
        } catch (Exception e) {
            ctx.response().setStatusCode(401).end("Invalid or expired token");
        }
    }
}
