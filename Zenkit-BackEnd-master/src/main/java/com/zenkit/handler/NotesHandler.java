package com.zenkit.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class NotesHandler {

    private final MongoClient mongo;
    private static final String COLLECTION = "notes";

    public NotesHandler(MongoClient mongo) {
        this.mongo = mongo;
    }

    public void getNotes(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("userId");
        JsonObject query = new JsonObject().put("userId", userId);

        mongo.find(COLLECTION, query, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonArray(res.result()).encode());
            } else {
                ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Failed to fetch notes").encode());
            }
        });
    }

    public void addNote(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("userId");
        JsonObject note = ctx.body().asJsonObject();
        note.put("userId", userId);

        mongo.insert(COLLECTION, note, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(201)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Note added successfully").encode());
            } else {
                ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Failed to add note").encode());
            }
        });
    }

    public void updateNote(RoutingContext ctx) {
        String noteId = ctx.pathParam("id");
        JsonObject updates = new JsonObject().put("$set", ctx.body().asJsonObject());
        JsonObject query = new JsonObject().put("_id", noteId);

        mongo.updateCollection(COLLECTION, query, updates, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Note updated").encode());
            } else {
                ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Failed to update note").encode());
            }
        });
    }

    public void deleteNote(RoutingContext ctx) {
        String noteId = ctx.pathParam("id");
        JsonObject query = new JsonObject().put("_id", noteId);

        mongo.removeDocument(COLLECTION, query, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Note deleted").encode());
            } else {
                ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Failed to delete note").encode());
            }
        });
    }

    public void deleteMultipleNotes(RoutingContext ctx) {
        JsonArray ids = ctx.body().asJsonObject().getJsonArray("ids");
        JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", ids));

        mongo.removeDocuments(COLLECTION, query, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("message", "Notes deleted").encode());
            } else {
                ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("error", "Failed to delete notes").encode());
            }
        });
    }
}
