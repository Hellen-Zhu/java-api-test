package com.api.common.listeners;

import com.api.data.SessionFactory;
import com.api.common.enums.XmlSuiteDetailAttribute;
import com.api.common.helpers.ListenerHelper;
import com.api.common.utils.MemoryCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.testng.ISuite;

@Slf4j
public class XmlSuiteListener extends TestNGXmlSuiteListener {

    @Override
    public void onStart(ISuite suite) {
        log.info("[TestNG] suite " + suite.getParameter(XmlSuiteDetailAttribute.COMPONENT.getName()) + " starts to run.");
        ListenerHelper.initialAutoProgressForSuiteRun(suite);
        ListenerHelper.initialAutoTestNGResultForSingleIdRun(suite);

    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            log.info("Starting suite cleanup for: " + suite.getName());
            MemoryCacheUtil.getInstance().invalidateAll();
            log.info("Memory cache cleared");
            
            ListenerHelper.endAutoProgressForSuiteRun(suite);
            log.info("Auto progress ended");
            
            ListenerHelper.sendCompleteSignalForModule(suite);
            log.info("Complete signal sent");
            
            ListenerHelper.endAutoTestNGResultForSingleIdRun(suite, buildFeatureScenarioMap(suite.getResults()));
            log.info("TestNG result processing completed");
            
            super.onFinish(suite);
            log.info("Suite cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during suite cleanup: " + e.getMessage(), e);
            // Ensure database connections are cleaned even if an exception occurs
            try {
                SessionFactory.closeAllSessions();
            } catch (Exception cleanupEx) {
                log.error("Failed to cleanup database sessions: " + cleanupEx.getMessage(), cleanupEx);
            }
        } finally {
            log.info("Database connections closed successfully");
            
            // Ensure the test process can end normally
            log.info("Suite cleanup completed. Forcing system cleanup...");
            
            // Force cleanup of any resources that might prevent process termination
            try {
                System.gc(); // Suggest garbage collection
                Thread.sleep(100); // Give the system a moment to clean up
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}