package com.example.qscheduler;

import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class KPIJobWatcher {
    private static final Logger logger = LoggerFactory.getLogger(KPIJobWatcher.class);

    Scheduler kpiScheduler;
    public KPIJobWatcher(Scheduler s) {
        kpiScheduler = s;
    }

    /**
     * run KPIJobWatcher
     * @throws Exception
     */
    public void run() throws Exception {

        try {
            // Setting the KPI Job factory of observerScheduler
            KPIJobFactory jf = new KPIJobFactory((StdScheduler)kpiScheduler);
            kpiScheduler.setJobFactory(jf);

            // Scheduling KPI Observer Job
            JobDetail observerJob = newJob(KPIObserverJob.class)
                    .withIdentity("observerJob", "observergroup")
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity(observerJob.getKey() + "_trigger", "observergroup")
                    .withSchedule(org.quartz.SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            Date ft = kpiScheduler.scheduleJob(observerJob, trigger);
            logger.info(observerJob.getKey() + " has been scheduled to run at: " + ft);

            // Starting KPI Observer Scheduler
            kpiScheduler.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
