/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.contoller;

import com.shion1305.ynufes.bodytemp2022.config.InstanceData;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

@WebListener
public class ProcessorManager implements ServletContextListener {
    static Logger logger = Logger.getLogger("ProcessManager");
    static HashMap<String, RequestProcessor> processors = new HashMap<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        init();
    }

    private static void init() {
        InstanceData[] data = new InstanceData[5];
        for (int i = 0; i < 5; i++) {
            data[i] = new InstanceData("TEST" + String.valueOf(i), true);
            processors.put("TEST" + String.valueOf(i), new RequestProcessor(data[i]));
        }
    }

    static void reload() {
        InstanceData[] data = new InstanceData[5];
        for (int i = 0; i < 5; i++) {
            data[i] = new InstanceData("TEST" + String.valueOf(i), false);
        }
        var newProcessors = new HashMap<String, RequestProcessor>();
        for (InstanceData d : data) {
            if (processors.containsKey(d.processName)) {
                var process = processors.get(d.processName);
                if (process.reload(d)) {
                    newProcessors.put(d.processName, process);
                    logger.info(String.format("[%s]Process Reloaded", d.processName));
                    continue;
                }
            }
            newProcessors.put(d.processName, new RequestProcessor(d));
            logger.info(String.format("[%s]Process Registered", d.processName));
        }
        processors.clear();
        processors = newProcessors;
    }

    public static void broadcastReminder() {
        for (var processor : processors.values()) {
            processor.broadcastReminder();
        }
    }

    public static class StatusDataManager {
        private static StatusDataGroup data;

        public synchronized static StatusDataGroup getStatusData() {
            data = updateStatusData();
            return data;
        }

        private static StatusDataGroup updateStatusData() {
            StatusDataGroup newData = new StatusDataGroup();
            processors.values().stream().parallel().forEach(p ->
                    newData.addStatus(new StatusData(p.getProcessName(), p.isEnabled()))
            );
            return newData;
        }
    }

    public static class StatusDataGroup {
        public final ArrayList<StatusData> data = new ArrayList<>();

        public void addStatus(StatusData d) {
            data.add(d);
        }
    }

    public static class StatusData {
        public String processName;
        public boolean enabled;

        public StatusData(String processName, boolean enabled) {
            this.processName = processName;
            this.enabled = enabled;
        }
    }
}
