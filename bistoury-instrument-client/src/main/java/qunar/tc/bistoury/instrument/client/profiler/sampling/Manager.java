package qunar.tc.bistoury.instrument.client.profiler.sampling;

import com.taobao.middleware.logger.Logger;
import qunar.tc.bistoury.attach.common.BistouryLoggger;
import qunar.tc.bistoury.instrument.client.profiler.AgentProfilerContext;
import qunar.tc.bistoury.instrument.client.profiler.ProfilerConstants;
import qunar.tc.bistoury.instrument.client.profiler.sampling.task.DumpTask;
import qunar.tc.bistoury.instrument.client.profiler.sampling.task.ProfilerTask;
import qunar.tc.bistoury.instrument.client.profiler.sampling.task.Task;
import qunar.tc.bistoury.instrument.client.profiler.util.trie.Trie;

import java.io.File;

/**
 * @author cai.wen created on 2019/10/17 10:51
 */
public class Manager {

    private static final Logger logger = BistouryLoggger.getLogger();

    private static final boolean isDebugMode = true;

    public static final String profilerThreadPoolName = "bistoury-profile";

    public static final String profilerThreadPoolDumpName = "bistoury-profile-dump";

    private static final String runnableDataPath = "runnable-traces.txt";
    private static final String filterRunnableDataPath = "filter-runnable-traces.txt";
    private static final String blockedDataPath = "blocked-traces.txt";
    private static final String filterBlockedDataPath = "filter-blocked-traces.txt";
    private static final String timedWaitingDataPath = "timed-waiting-traces.txt";
    private static final String filterTimedWaitingDataPath = "filter-timed-waiting-traces.txt";
    private static final String waitingDataPath = "waiting-traces.txt";
    private static final String filterWaitingDataPath = "filter-waiting-traces.txt";
    private static final String allStatePath = "all-state-traces.txt";
    private static final String filterAllStatePath = "filter-all-state-traces.txt";

    private static volatile String profilerId;

    private static final Trie compactPrefixPackage = new Trie();

    static {
        compactPrefixPackage.insert("java.");
        compactPrefixPackage.insert("javax.");
        compactPrefixPackage.insert("sun.");
        compactPrefixPackage.insert("org.springframework.");
        compactPrefixPackage.insert("org.jboss.");
        compactPrefixPackage.insert("org.apache.");
        compactPrefixPackage.insert("com.sun.");
        compactPrefixPackage.insert("org.mybatis.");
        compactPrefixPackage.insert("com.mysql.");
        compactPrefixPackage.insert("io.netty.");
        compactPrefixPackage.insert("com.google.");
        compactPrefixPackage.insert("ch.qos.");
        compactPrefixPackage.insert("org.slf4j.");
        compactPrefixPackage.insert("io.termd.core.");
    }

    public static boolean isCompactClass(String className) {
        return compactPrefixPackage.containsPrefixNode(className);
    }

    private static Task profilerTask;

    private static Task dumpTask;

    private static void createDumpPath(String tempDir) {
        ProfilerConstants.PROFILER_ROOT_PATH = tempDir + File.separator + "bistoury-profiler";
        ProfilerConstants.PROFILER_TEMP_PATH = tempDir + File.separator + "bistoury-profiler" + File.separator + "tmp";

        if (isDebugMode) {
            new File(ProfilerConstants.PROFILER_ROOT_PATH).delete();
            new File(ProfilerConstants.PROFILER_TEMP_PATH).delete();
        }
        new File(ProfilerConstants.PROFILER_ROOT_PATH).mkdirs();
        new File(ProfilerConstants.PROFILER_TEMP_PATH + File.separator + profilerId).mkdirs();
    }

    public static synchronized void init(int durationSeconds, int frequencyMillis, String profilerId, String tempDir) {
        Manager.profilerId = profilerId;
        AgentProfilerContext.setProfilerId(profilerId);
        profilerTask = new ProfilerTask(frequencyMillis);
        dumpTask = new DumpTask(durationSeconds);
        createDumpPath(tempDir);

        profilerTask.init();
        dumpTask.init();
        AgentProfilerContext.startProfiling();
    }


    public synchronized static void stop() {
        checkProfilerState();

        stopTask(profilerTask);
        stopTask(dumpTask);
        AgentProfilerContext.stopProfiling();
    }

    public static void renameResult() {
        File preDumpPath = new File(ProfilerConstants.PROFILER_TEMP_PATH + File.separator + profilerId);
        File realDumpPath = new File(ProfilerConstants.PROFILER_ROOT_PATH + File.separator + profilerId);
        preDumpPath.renameTo(realDumpPath);
    }

    private static void checkProfilerState() {
//        long startTime = AgentProfilerContext.getStartTime();
//        long curMillis = System.currentTimeMillis();
//        long duration = curMillis - startTime;
//        if (duration < ProfilerConstants.MIN_DURATION_MILLIS) {
//            String detailMsg = "profiler duration is too short. duration: " + duration / 1000 +
//                    "s. min duration must " + ProfilerConstants.MIN_DURATION_MILLIS / 1000 + "s";
//            throw new IllegalStateException(detailMsg);
//        }
    }

    private static void stopTask(Task task) {
        try {
            if (task != null) {
                task.stop();
            }
        } catch (Exception e) {
            logger.error("", "destroy task error.", e);
        }
    }

    public static boolean isDebugMode() {
        return isDebugMode;
    }

    private static String getFullPath(String fileName) {
        String profilerIdPath = ProfilerConstants.PROFILER_TEMP_PATH + File.separator + profilerId;
        return profilerIdPath + File.separator + fileName;
    }

    public static String getRunnableDataPath() {
        return getFullPath(runnableDataPath);
    }

    public static String getFilterRunnableDataPath() {
        return getFullPath(filterRunnableDataPath);
    }

    public static String getBlockedDataPath() {
        return getFullPath(blockedDataPath);
    }

    public static String getFilterBlockedDataPath() {
        return getFullPath(filterBlockedDataPath);
    }

    public static String getTimedWaitingDataPath() {
        return getFullPath(timedWaitingDataPath);
    }

    public static String getFilterTimedWaitingDataPath() {
        return getFullPath(filterTimedWaitingDataPath);
    }

    public static String getWaitingDataPath() {
        return getFullPath(waitingDataPath);
    }

    public static String getFilterWaitingDataPath() {
        return getFullPath(filterWaitingDataPath);
    }

    public static String getAllStatePath() {
        return getFullPath(allStatePath);
    }

    public static String getFilterAllStatePath() {
        return getFullPath(filterAllStatePath);
    }
}
