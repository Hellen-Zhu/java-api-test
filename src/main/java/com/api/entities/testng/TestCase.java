package com.api.entities.testng;

import lombok.Data;

@Data
public class TestCase {

    String className;
    String name;
    long time;
    boolean ifPass;

}