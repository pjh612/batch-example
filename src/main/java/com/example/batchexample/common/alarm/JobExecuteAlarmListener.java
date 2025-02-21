package com.example.batchexample.common.alarm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JobExecuteAlarmListener implements JobExecutionListener {
    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;

    public JobExecuteAlarmListener(MessageSender messageSender, ObjectMapper objectMapper) {
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job execution completed.");
        Map<String, Object> objectMap = new HashMap<>();
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        Duration duration = Duration.between(startTime, endTime);

        String alertMessage = "";
        StringBuilder stepSummary = new StringBuilder();
        if (status != BatchStatus.COMPLETED) {
            alertMessage += "<!channel> \n";
        }

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            stepSummary.append(String.format(
                    "Step: %s | Status: %s | Read: %d | Write: %d | Commit: %d | Skip: %d | Errors: %s\n",
                    stepExecution.getStepName(),
                    stepExecution.getStatus(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getCommitCount(),
                    stepExecution.getSkipCount(),
                    stepExecution.getFailureExceptions()
            ));
        }

        JobParameters jobParameters = jobExecution.getJobParameters();
        alertMessage += String.format(
                "Batch Job Summary\n" +
                        "-----------------\n" +
                        "Job Name: %s\n" +
                        "Status: %s\n" +
                        "Start Time: %s\n" +
                        "End Time: %s\n" +
                        "Duration: %s seconds\n" +
                        "Parameters: %s\n" +
                        "Exceptions: %s\n" +
                        "\nStep Details:\n%s",
                jobName,
                status,
                startTime,
                endTime,
                duration.getSeconds(),
                jobParameters.getParameters(),
                jobExecution.getAllFailureExceptions(),
                stepSummary
        );

        objectMap.put("text", alertMessage);
        objectMap.put("channel", "#batch_alarm");
        JsonNode jsonNode = objectMapper.convertValue(objectMap, JsonNode.class);
        messageSender.sendMessage(jsonNode);
    }
}
