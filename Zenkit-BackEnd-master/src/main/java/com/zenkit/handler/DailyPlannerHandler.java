package com.zenkit.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class DailyPlannerHandler {

    private final MongoClient client;

    public DailyPlannerHandler(MongoClient client) {
        this.client = client;
    }

    // GET /api/reminders?date=YYYY-MM-DD
    public void getTasks(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("userId");  // ✅ Fixed
        String date = ctx.request().getParam("date");

        JsonObject query = new JsonObject()
                .put("userId", userId)
                .put("date", date);

        client.find("daily_tasks", query, res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(res.result().toString());
            } else {
                ctx.response().setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    // POST /api/reminders
    public void addTask(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("userId");  // ✅ Fixed
        JsonObject task = ctx.body().asJsonObject();

        task.put("userId", userId);

        client.insert("daily_tasks", task, res -> {
            if (res.succeeded()) {
                task.put("_id", res.result()); // Add inserted ID to response
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(201)
                        .end(task.encode());
            } else {
                ctx.response().setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    // PUT /api/reminders/:id
    public void updateTask(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject updateData = ctx.body().asJsonObject();

        JsonObject query = new JsonObject().put("_id", id);
        JsonObject update = new JsonObject().put("$set", updateData);

        client.updateCollection("daily_tasks", query, update, res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end("{\"message\": \"Task updated\"}");
            } else {
                ctx.response().setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }

    // DELETE /api/reminders/:id
    public void deleteTask(RoutingContext ctx) {
        String id = ctx.pathParam("id");

        client.removeDocument("daily_tasks", new JsonObject().put("_id", id), res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end("{\"message\": \"Task deleted\"}");
            } else {
                ctx.response().setStatusCode(500).end(res.cause().getMessage());
            }
        });
    }
}
