package com.example.batchexample.common.alarm;

import com.fasterxml.jackson.databind.JsonNode;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.springframework.stereotype.Component;

@Component
public class SlackMessageSender implements MessageSender {
    private final SlackApi slackApi;

    public SlackMessageSender(SlackApi slackApi) {
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
