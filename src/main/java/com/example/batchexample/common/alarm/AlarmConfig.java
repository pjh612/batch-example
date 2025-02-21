package com.example.batchexample.common.alarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.gpedro.integrations.slack.SlackApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlarmConfig {

    @Bean
    public SlackApi slackApi(@Value("${slack.webhook.url}") String webhookUrl) {
        return new SlackApi(webhookUrl);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModules();
    }

}
