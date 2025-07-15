package com.zenkit.handler;

import com.zenkit.model.PomodoroLog;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class PomodoroHandler {

    private final MongoClient mongoClient;

    public PomodoroHandler(MongoClient mongoClient, Vertx vertx) {
        this.mongoClient = mongoClient;
    }

    public void logPomodoroSession(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        if (body == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body.");
            return;
        }

        PomodoroLog log = PomodoroLog.fromJson(body);

        mongoClient.save("pomodoro_logs", log.toJson(), res -> {
            if (res.succeeded()) {
                ctx.response()
                        .setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Pomodoro session logged").put("id", res.result()).encode());
            } else {
                ctx.response().setStatusCode(500).end("Failed to log session: " + res.cause().getMessage());
            }
        });
    }
}
