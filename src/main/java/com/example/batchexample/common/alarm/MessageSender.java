package com.example.batchexample.common.alarm;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

public interface MessageSender {
    void sendMessage(JsonNode jsonNode);
}
