package com.example.qscheduler;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class KPIJobFactory implements JobFactory {
    Scheduler kpiScheduler;
    public KPIJobFactory(Scheduler s)
    {
        kpiScheduler = s;
    }

    public KPIObserverJob newJob(TriggerFiredBundle bundle, Scheduler Scheduler) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<KPIObserverJob> jobClass = (Class<KPIObserverJob>) jobDetail.getJobClass();
        try {
            // this is how we construct our custom job with custom factory
            return jobClass.getConstructor(Scheduler.getClass()).newInstance(kpiScheduler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
