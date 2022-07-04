/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.contoller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@WebListener
public class ReminderSender implements ServletContextListener {

    static Logger logger = Logger.getLogger("TempInputReminder");
    static Timer timer = new Timer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        schedule();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        timer.purge();
    }

    public static void schedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        Date d = calendar.getTime();
        logger.info("Scheduled at " + d);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ProcessorManager.broadcastReminder();
                logger.info("The scheduled reminder has been broadcasted");
                schedule();
            }
        }, d);
    }
}
