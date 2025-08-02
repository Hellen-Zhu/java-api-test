package com.api.entities.fast;

import lombok.Data;

@Data
public class Result {
    long duration;
    String status;
    String error_message;
}