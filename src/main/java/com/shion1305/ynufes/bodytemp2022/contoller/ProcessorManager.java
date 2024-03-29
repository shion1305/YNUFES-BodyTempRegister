/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.contoller;

import com.linecorp.bot.model.event.Event;
import com.shion1305.ynufes.bodytemp2022.config.InstanceData;
import com.shion1305.ynufes.bodytemp2022.config.JsonConfigManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

@WebListener
public class ProcessorManager implements ServletContextListener {
    static Logger logger = Logger.getLogger("ProcessManager");
    static HashMap<String, RequestProcessor> processors = new HashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        init();
    }

    private static void init() {
        InstanceData[] data = JsonConfigManager.readJson();
        if (data == null) return;
        for (InstanceData d : data) {
            processors.put(d.processName, new RequestProcessor(d));
            logger.info(String.format("[%s]Process Registered", d.processName));
        }
    }


    /**
     * 設定の再読み込みを行う。
     * jsonファイルからボット設定が消去された場合、preferenceは削除されずにインスタンスのみ削除される。
     * reload処理であるが、reloadableではない(lineTokenが違う)場合は、preferenceが削除され、
     * インスタンスが再生成される。
     */
    static void reload() {
        InstanceData[] data = JsonConfigManager.readJson();
        if (data == null) return;
        var newProcessors = new HashMap<String, RequestProcessor>();
        for (InstanceData d : data) {
            if (processors.containsKey(d.processName)) {
                var process = processors.get(d.processName);
                if (process.reload(d)) {
                    newProcessors.put(d.processName, process);
                    logger.info(String.format("[%s]Process Reloaded, %b", d.processName, d.enabled));
                    continue;
                } else {
                    process.clearPreference();
                }
            }
            newProcessors.put(d.processName, new RequestProcessor(d));
            logger.info(String.format("[%s]Process Registered", d.processName));
        }
        processors.clear();
        processors = newProcessors;
    }

    public static void processEvent(String reqName, Event e) throws BackingStoreException, IOException {
        RequestProcessor processor = processors.get(reqName);
        if (processor != null) processor.processEvent(e);
        else logger.info("Received unknown event: reqName: " + reqName);
    }

    public static void broadcastReminder() {
        for (var processor : processors.values()) {
            processor.broadcastReminder();
        }
    }

    public synchronized static void checkNoSubmission() throws BackingStoreException, IOException {
        for (var processor : processors.values()) {
            processor.checkNoSubmission();
        }
    }


    public static class StatusDataManager {
        private static StatusDataGroup data;

        public synchronized static StatusDataGroup getStatusData() {
            if (data == null || System.currentTimeMillis() - data.time > 60000) {
                data = updateStatusData();
                logger.info("UPDATED");
            }
            return data;
        }

        private static StatusDataGroup updateStatusData() {
            StatusDataGroup newData = new StatusDataGroup();
            processors.values().stream().parallel().forEach(p -> {
                        StatusData statusData = new StatusData(p.getProcessName(), p.isEnabled(), p.getLineUsage(), p.getRegisteredNum());
                        p.requestNumFollowers(statusData);
                        newData.addStatus(statusData);
                    }
            );
            newData.time = System.currentTimeMillis();
            return newData;
        }
    }

    public static class StatusDataGroup {
        public final ArrayList<StatusData> data = new ArrayList<>();
        long time = 0L;

        public void addStatus(StatusData d) {
            data.add(d);
        }
    }

    public static class StatusData {
        public String processName;
        public boolean enabled;
        public long usage;
        public long registered;
        public LineNumInfo numFollowers = new LineNumInfo();

        public StatusData(String processName, boolean enabled, long usage, long registered) {
            this.processName = processName;
            this.enabled = enabled;
            this.usage = usage;
            this.registered = registered;
        }

        public static class LineNumInfo {
            public enum Status {
                READY,
                NOT_READY,
                ERROR,
                PROCESSING
            }

            public Status status = Status.PROCESSING;
            public long followers;
            public long blockers;
            public long targetReaches;
        }
    }
}
