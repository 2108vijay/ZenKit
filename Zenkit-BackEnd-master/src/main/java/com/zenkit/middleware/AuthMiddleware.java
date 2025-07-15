package com.zenkit.middleware;

import com.zenkit.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import io.vertx.ext.web.RoutingContext;

public class AuthMiddleware {

    public void handle(RoutingContext ctx) {
        String authHeader = ctx.request().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.response().setStatusCode(401).end("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            Claims claims = JwtUtil.verifyToken(token);
            ctx.put("userId", claims.getSubject());
            ctx.put("email", claims.get("email"));
            ctx.next(); // Proceed to the actual handler
        } catch (ExpiredJwtException e) {
            ctx.response().setStatusCode(401).end("Token expired");
        } catch (SignatureException e) {
            ctx.response().setStatusCode(401).end("Invalid token signature");
        } catch (Exception e) {
            ctx.response().setStatusCode(401).end("Token verification failed");
        }
    }
}
