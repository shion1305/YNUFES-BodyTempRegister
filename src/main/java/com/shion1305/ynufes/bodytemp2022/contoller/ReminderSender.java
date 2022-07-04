/*
 * Copyright (c) 2022 Shion Ichikawa All Rights Reserved.
 */

package com.shion1305.ynufes.bodytemp2022.contoller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

@WebListener
public class ReminderSender implements ServletContextListener {

    static Logger logger = Logger.getLogger("TempInputReminder");
    /*
    Schedule at next 07:00
     */
    static Timer timer = new Timer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        schedule();
        scheduleLateReminder();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        timer.purge();
    }

    public static void schedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
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

    //15時と20時に再度リマインド
    public static void scheduleLateReminder() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 15) {
            calendar.set(Calendar.HOUR_OF_DAY, 15);
        } else if (hour < 20) {
            calendar.set(Calendar.HOUR_OF_DAY, 20);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date d1 = calendar.getTime();
        logger.info("Late Reminder Scheduled at " + d1);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ProcessorManager.checkNoSubmission();
                } catch (BackingStoreException | IOException e) {
                    throw new RuntimeException(e);
                }
                scheduleLateReminder();
                logger.info("Scheduled late reminder");
            }
        }, d1);
    }
}
