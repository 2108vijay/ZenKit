package com.zenkit;

import com.zenkit.handler.AuthHandler;
import com.zenkit.middleware.AuthMiddleware;
import com.zenkit.router.AuthRouter;
import com.zenkit.router.DailyPlannerRouter;
import com.zenkit.router.NotesRouter;
import com.zenkit.router.PomodoroRouter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class App extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new App());
    }

    @Override
    public void start() {
        try {
            System.out.println("🟢 Starting Vert.x application...");

            JsonObject mongoConfig = new JsonObject()
                    .put("connection_string", "mongodb://localhost:27017")
                    .put("db_name", "zenkit");

            MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);
            AuthHandler authHandler = new AuthHandler(mongoClient, vertx);
            AuthMiddleware authMiddleware = new AuthMiddleware();
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());
            router.route().handler(CorsHandler.create("*")
                    .allowedMethod(HttpMethod.GET)
                    .allowedMethod(HttpMethod.POST)
                    .allowedMethod(HttpMethod.PUT)
                    .allowedMethod(HttpMethod.DELETE)
                    .allowedMethod(HttpMethod.OPTIONS)
                    .allowedHeader("Content-Type")
                    .allowedHeader("Authorization"));

            AuthRouter.setup(router, authHandler,authMiddleware);
            DailyPlannerRouter.setup(router, mongoClient);
            PomodoroRouter.setup(router, mongoClient, vertx);
            NotesRouter.setup(router, mongoClient); // ✅ Only mongoClient

            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(8888, http -> {
                        if (http.succeeded()) {
                            System.out.println("✅ Server running at http://localhost:8888");
                        } else {
                            System.err.println("❌ Failed to start server: " + http.cause().getMessage());
                            http.cause().printStackTrace();
                        }
                    });

        } catch (Exception e) {
            System.err.println("❌ Error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
