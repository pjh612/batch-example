package com.example.batchexample.common.alarm;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.springframework.stereotype.Component;

@Component
public class MessageSenderImpl implements MessageSender {
    private final SlackApi slackApi;

    public MessageSenderImpl(SlackApi slackApi) {
        this.slackApi = slackApi;
    }

    @Override
    public void sendMessage(JsonNode payload) {
        String channel = payload.get("channel").asText();
        String text = payload.get("text").asText();

        SlackMessage slackMessage = new SlackMessage(channel, text);

        slackApi.call(slackMessage);
    }
}
