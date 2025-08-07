package com.api.listeners;

import com.api.entities.testng.XmlSuiteDetailAttribute;
import com.api.helpers.ListenerHelper;
import com.api.utils.MemoryCacheUtil;
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
            // 确保即使在异常情况下，数据库连接也得到清理
            try {
                com.api.helpers.SessionFactory.closeAllSessions();
            } catch (Exception cleanupEx) {
                log.error("Failed to cleanup database sessions: " + cleanupEx.getMessage(), cleanupEx);
            }
        } finally {
            log.info("Database connections closed successfully");
            
            // 确保测试进程能够正常结束
            log.info("Suite cleanup completed. Forcing system cleanup...");
            
            // 强制清理任何可能阻止进程结束的资源
            try {
                System.gc(); // 建议垃圾回收
                Thread.sleep(100); // 给系统一点时间清理
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}