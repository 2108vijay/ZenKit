package com.zenkit.router;

import com.zenkit.handler.NotesHandler;
import com.zenkit.util.JwtUtil;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;

public class NotesRouter {
    public static void setup(Router router, MongoClient mongo) {
        NotesHandler notesHandler = new NotesHandler(mongo);

        router.get("/api/auth/notes").handler(JwtUtil::extractUser).handler(notesHandler::getNotes);
        router.post("/api/auth/notes").handler(JwtUtil::extractUser).handler(notesHandler::addNote);
        router.put("/api/auth/notes/:id").handler(JwtUtil::extractUser).handler(notesHandler::updateNote);
        router.delete("/api/auth/notes/:id").handler(JwtUtil::extractUser).handler(notesHandler::deleteNote);
        router.post("/api/auth/notes/delete-many").handler(JwtUtil::extractUser).handler(notesHandler::deleteMultipleNotes);
    }
}
