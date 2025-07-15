package com.zenkit.router;

import com.zenkit.handler.PomodoroHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

public class PomodoroRouter {

    public static void setup(Router router, MongoClient mongoClient, Vertx vertx) {
        PomodoroHandler handler = new PomodoroHandler(mongoClient, vertx);

        // Route to log Pomodoro session
        router.post("/api/pomodoro").handler(handler::logPomodoroSession);
    }
}
