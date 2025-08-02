package com.api.entities;

import lombok.Data;

@Data
public class AutomationResult {
    int returnCode;
    String returnMessage;
    private Object returnBody;

    public AutomationResult() {
        this.returnCode = 0;
        this.returnMessage = "";
        this.returnBody = null;
    }

    public AutomationResult(Integer returnCode, String returnMessage, Object returnBody) {
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
        this.returnBody = returnBody;
    }

    public AutomationResult(Integer returnCode, String returnMessage) {
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
        this.returnBody = null;
    }

    public AutomationResult(Integer returnCode, Object returnBody) {
        this.returnCode = returnCode;
        this.returnMessage = "";
        this.returnBody = returnBody;
    }
}