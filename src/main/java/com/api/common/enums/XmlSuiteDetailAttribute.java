package com.api.common.enums;

// File: XmlSuiteDetailAttribute.java
import com.api.common.annotations.CommentAnnotation;

@CommentAnnotation(description = "Used to set Attributes for TestNG Suite")
public enum XmlSuiteDetailAttribute {

    DEBUG_LOG("debug"),
    REPORT_LOG("report"),


    @CommentAnnotation(description = "[Switch] which case need to run, can self-defined, {Boolean}")
    SANITY_ONLY("sanityOnly"),

    @CommentAnnotation(description = "[Switch] If true then show debug log without upload and send action, else then only show report log and do upload and send, {Boolean}")
    IS_DEBUG("isDebug"),

    @CommentAnnotation(description = "[Switch] Decide what actualFixVersion is, if true -> releaseVersion, else -> dailyrunVersion, {Boolean}")
    IS_RELEASE("isRelease"),

    @CommentAnnotation(description = "[Case] Decide which test cases need to run, Module -> component -> scenario -> id, {String}")
    MODULE("module"),

    @CommentAnnotation(description = "[Case] Decide which test cases need to run, Module -> component -> scenario -> id, {String}")
    COMPONENT("component"),

    @CommentAnnotation(description = "[Case] Decide which test cases need to run, Module -> component -> scenario -> id, {Set<String>}")
    SCENARIO_LIST("scenarios"),

    @CommentAnnotation(description = "[Case] Decide which test cases need to run, Module -> component -> scenario -> id, {Set<String>}")
    SCENARIO("scenario"),

    @CommentAnnotation(description = "[Case] Decide which test cases need to run, Module -> component -> scenario -> id, {Set<Integer>}")
    ID_LIST("ids"),

    @CommentAnnotation(description = "[Case] Decide which test cases need to run, Module -> component -> scenario -> id - label, {Set<String>}")
    LABEL_LIST("labels"),

    @CommentAnnotation(description = "[Info] VersionID for fixVersion, {String}")
    VERSION_ID("versionId"),
    @CommentAnnotation(description = "[Info] Group Name on Fast, Project -> Group -> Suite -> Feature[], {String}")
    GROUP("group"),
    @CommentAnnotation(description = "[Info] Suite Name on Fast, Project -> Group -> Suite -> Feature[], {String}")
    SUITE("suite"),

    @CommentAnnotation(description = "[Info] Only label which will show on Dashboard, {Regression}, {String}")
    TEST_SCOPE("testScope"),

    @CommentAnnotation(description = "[Info] Only label which will show on Dashboard, {Regression}, {String}")
    TEST_TYPE("testType"),

    @CommentAnnotation(description = "[Info] Only label which will show on Dashboard, {String}")
    REGION("region"),

    @CommentAnnotation(description = "[Info] Only label which will show on Dashboard, {String}")
    QA("qa"),

    @CommentAnnotation(description = "[Info] Only label which will show on Dashboard, {String}")
    RUN_BY("runBy"),

    @CommentAnnotation(description = "[Info] Only label which will show on Dashboard, {String}")
    BUILD("build"),

    @CommentAnnotation(description = "[Info] The Default FixVersion for Daily Run, {String}")
    DAILYRUN_VERSION("dailyRunVersion"),

    @CommentAnnotation(description = "[Info] The Release Version, {Set<String>}")
    RELEASE_VERSION_LIST("releaseVersionList"),

    @CommentAnnotation(description = "[Info] The Actual FixVersion during the real RUNTIME, {String}")
    ACTUAL_FIXVERSION("actualFixVersion"),

    @CommentAnnotation(description = "[Info] Others, {String}")
    RUN_ID("runId");

    private final String name;

    /**
     * Constructor for the enum.
     * @param name The string value associated with the enum constant.
     */
    XmlSuiteDetailAttribute(String name) {
        this.name = name;
    }

    /**
     * @return The string value of the attribute.
     */
    public String getName() {
        return name;
    }
}