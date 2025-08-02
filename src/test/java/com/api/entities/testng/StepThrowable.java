package com.api.entities.testng;

import lombok.Data;

@Data
public class StepThrowable {

    private String message;
    private String localizedMessage;
    private String stackTrace;

    public StepThrowable(Throwable throwable) {
        if (throwable != null) {
            this.message = throwable.getMessage();
            this.localizedMessage = throwable.getLocalizedMessage();
            StringBuilder stackTraceBuilder = new StringBuilder("Stack Trace:");
            stackTraceBuilder.append(System.lineSeparator());
            int i = 0;
            for (StackTraceElement line : throwable.getStackTrace()) {
                if (i < 1) {
                    stackTraceBuilder.append(line.toString());
                    stackTraceBuilder.append(System.lineSeparator());
                }
                i++;
            }
            this.stackTrace = stackTraceBuilder.toString();
        }
    }

}