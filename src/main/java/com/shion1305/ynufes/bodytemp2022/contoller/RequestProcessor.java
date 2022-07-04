/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.contoller;

import com.shion1305.ynufes.bodytemp2022.config.InstanceData;

import java.util.logging.Logger;

public class RequestProcessor {
    private final Logger logger;
    private volatile InstanceData data;

    public RequestProcessor(InstanceData data) {
        this.logger = Logger.getLogger("RequestProcessor{" + data.processName + "}");
        init(data);
    }

    private void init(InstanceData data) {
        this.data = data;
    }

    public boolean isReloadable(InstanceData d) {
        return d.processName.equals(data.processName);
    }

    public boolean reload(InstanceData newData) {
        if (!isReloadable(newData)) return false;
        if (data.enabled && !newData.enabled) {
            logger.info("DISABLED: " + data.processName);
        } else if (!data.enabled && newData.enabled) {
            logger.info("ENABLED: " + data.processName);
        }
        init(newData);
        return true;
    }

    public void broadcastReminder() {
        logger.info(data.processName + ": " + isEnabled());
        if (!isEnabled()) return;
        logger.info("Enable:TRUE "+data.processName);
    }

    public String getProcessName() {
        return data.processName;
    }

    public boolean isEnabled() {
        return data.enabled;
    }
}
