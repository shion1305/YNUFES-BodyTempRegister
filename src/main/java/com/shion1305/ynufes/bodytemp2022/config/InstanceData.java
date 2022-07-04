/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.config;


public class InstanceData {
    public String processName;
    public boolean enabled;

    public InstanceData(String name, boolean enabled) {
        this.processName = name;
        this.enabled = enabled;
    }
}
