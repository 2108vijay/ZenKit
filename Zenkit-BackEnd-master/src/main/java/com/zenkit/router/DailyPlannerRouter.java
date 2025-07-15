package com.zenkit.router;

import com.zenkit.handler.DailyPlannerHandler;
import com.zenkit.util.JwtMiddleware;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

public class DailyPlannerRouter {
    public static void setup(Router router, MongoClient mongoClient) {
        DailyPlannerHandler handler = new DailyPlannerHandler(mongoClient);

        router.route("/api/reminders/*").handler(JwtMiddleware.handle());

        router.get("/api/reminders").handler(handler::getTasks);
        router.post("/api/reminders").handler(handler::addTask);
        router.put("/api/reminders/:id").handler(handler::updateTask);
        router.delete("/api/reminders/:id").handler(handler::deleteTask);
    }
}
