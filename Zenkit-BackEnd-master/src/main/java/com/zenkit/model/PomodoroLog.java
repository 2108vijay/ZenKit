package com.zenkit.model;

import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;

public class PomodoroLog {
    private String id;
    private String userId;
    private String startTime;
    private String endTime;
    private int durationMinutes;
    private String label;

    public PomodoroLog() {
    }

    public PomodoroLog(String id, String userId, String startTime, String endTime, int durationMinutes, String label) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.label = label;
    }

    public static PomodoroLog fromJson(JsonObject json) {
        PomodoroLog log = new PomodoroLog();
        if (json.containsKey("_id")) {
            Object idObj = json.getValue("_id");
            if (idObj instanceof JsonObject && ((JsonObject) idObj).containsKey("$oid")) {
                log.setId(((JsonObject) idObj).getString("$oid"));
            } else {
                log.setId(idObj.toString());
            }
        }
        log.setUserId(json.getString("userId"));
        log.setStartTime(json.getString("startTime"));
        log.setEndTime(json.getString("endTime"));
        log.setDurationMinutes(json.getInteger("durationMinutes", 0));
        log.setLabel(json.getString("label"));
        return log;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (id != null && !id.isEmpty()) {
            json.put("_id", id);
        }
        json.put("userId", userId);
        json.put("startTime", startTime);
        json.put("endTime", endTime);
        json.put("durationMinutes", durationMinutes);
        json.put("label", label);
        return json;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
