package com.api.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/*
 * Purpose: Provide descriptive comments for enum values and classes
 * Usage: Primarily for documentation generation, configuration description, and metadata management
 * Retention: Runtime (@Retention(RetentionPolicy.RUNTIME))
 * Scenarios: Configuration property description, API documentation generation, test reporting
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommentAnnotation {

    String description();

}