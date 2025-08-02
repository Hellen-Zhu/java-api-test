package com.api.entities.testng;

// File: XmlSuiteDetailAttribute.java
import com.api.annotations.CommentAnnotation;

@CommentAnnotation(description = "Used to set Attributes for TestNG Suite")
public enum XmlSuiteDetailAttribute {

    DEBUG_LOG("debug"),
    REPORT_LOG("report"),

    @CommentAnnotation(description = "[Switch] Fast Dashboard Upload, {Boolean}")
    ENABLE_FAST("enableFast"),

    @CommentAnnotation(description = "[Switch] Mondo Dashboard Upload, {Boolean}")
    ENABLE_MONDO("enableMondo"),

    @CommentAnnotation(description = "[Switch] Test Cycle Upload, {Boolean}")
    ENABLE_JIRA("enableJira"),

    @CommentAnnotation(description = "[Switch] Email Send Upload, {Boolean}")
    ENABLE_EMAIL("enableEmail"),

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

    @CommentAnnotation(description = "[Info] Project Key on Mondo, {String}")
    PROJECT_KEY("projectKey"),

    @CommentAnnotation(description = "[Info] Project Key on Jira, {String}")
    JIRA_KEY("jiraKey"),

    @CommentAnnotation(description = "[Info] Project Key on Fast, {String}")
    FAST_KEY("fastKey"),

    @CommentAnnotation(description = "[Info] Project Name on Fast, Project -> Group -> Suite -> Feature[], {String}")
    FAST_PROJECT("fastProject"),

    @CommentAnnotation(description = "[Info] VersionID for fixVersion, {String}")
    VERSION_ID("versionId"),

    @CommentAnnotation(description = "[Info] Group Name on Fast, Project -> Group -> Suite -> Feature[], {String}")
    GROUP("group"),

    @CommentAnnotation(description = "[Info] Suite Name on Fast, Project -> Group -> Suite -> Feature[], {String}")
    SUITE("suite"),

    @CommentAnnotation(description = "[Info] ENV for Fast Dashboard, {DEV,UAT}, {String}")
    FAST_DASHBOARD_ENV("fastDashboardEnv"),

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

    @CommentAnnotation(description = "[Info] Receiver Address for Email Send, {Set<String>}")
    RECEIVER_ADDRESS("receiverAddress"),

    @CommentAnnotation(description = "[Info] Receiver Address for Email Send, {Set<String>}")
    COPY_ADDRESS("copyAddress"),

    @CommentAnnotation(description = "[Info] The Default FixVersion for Daily Run, {String}")
    DAILYRUN_VERSION("dailyRunVersion"),

    @CommentAnnotation(description = "[Info] The Release Version, {Set<String>}")
    RELEASE_VERSION_LIST("releaseVersionList"),

    @CommentAnnotation(description = "[Info] The Actual FixVersion during the real RUNTIME, {String}")
    ACTUAL_FIXVERSION("actualFixVersion"),

    @CommentAnnotation(description = "[Info] Test Cycle Name for TestCycle Upload to Jira, {String}")
    TEST_CYCLE("testCycle"),

    @CommentAnnotation(description = "[Info] Token for Jira login, {String}")
    JIRA_TOKEN("jiraToken"),

    @CommentAnnotation(description = "[Info] Token for Jira login, {String}")
    USERNAME("username"),

    @CommentAnnotation(description = "[Info] Token for Jira login, {String}")
    PASSWORD("password"),

    @CommentAnnotation(description = "[Info] Others, {String}")
    RUN_ID("runId"),

    AUTOMATION_TOOL_URL("automation-tool.service.url"); // Semicolon is required before fields and methods

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