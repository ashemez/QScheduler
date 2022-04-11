package com.example.qscheduler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KPIObserverJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(KPIObserverJob.class);

    List<JobDetail> jobList;
    Scheduler kpiScheduler;

    public KPIObserverJob(StdScheduler s) {
        kpiScheduler = s;
    }

    List<String> scheduledJobList;
    HashMap<String, JobDetail> alreadyScheduledJobList;

    String cronFormat = "SECOND MINUTE HOUR DAY_OF_MON MONTH DAY_OF_WEEK";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        scheduledJobList = new ArrayList<String>();
        alreadyScheduledJobList = new HashMap<String, JobDetail>();
        //JobKey jobKey = context.getJobDetail().getKey();

        jobList = new ArrayList<JobDetail>();

        // get all KPIs and create their jobs
        CreateJobs();

        // get the list of currently scheduled kpi jobs
        try {
            for (String groupName : kpiScheduler.getJobGroupNames()) {
                for (JobKey jk : kpiScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    String jobName = jk.getName();
                    String jobGroup = jk.getGroup();

                    scheduledJobList.add(jobName);

                    JobDetail jd = kpiScheduler.getJobDetail(jk);
                    alreadyScheduledJobList.put(jobName, jd);

                    logger.info("already scheduled jobName {}", jobName);

                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        // schedule or unschedule KPI jobs if not done yet
        for(JobDetail job : jobList) {
            try {
                if(!scheduledJobList.contains(job.getKey().getName()))
                {
                    if(job.getJobDataMap().getInt("isRunning") == 1)
                    {
                        logger.info("scheduling job: kpiJobName_{}", job.getJobDataMap().getString("kpiName"));
                        ScheduleJob(job);
                    }
                } else {

                    // check any changes in the KPI job definition
                    JobDetail sJD = alreadyScheduledJobList.get("kpiJobName_" + job.getJobDataMap().getString("kpiName"));
                    if(!job.getJobDataMap().getString("cron").equals(sJD.getJobDataMap().getString("cron"))) {
                        logger.info("rescheduling job: kpiJobName {} , new cron: {}",
                                job.getJobDataMap().getString("kpiName"), job.getJobDataMap().getString("cron"));
                        UnscheduleJob(job.getJobDataMap().getString("kpiName"));
                        ScheduleJob(job);
                    }

                    if(job.getJobDataMap().getInt("isRunning") == 0) {
                        logger.info("Unscheduling: kpiJobName {}" + job.getJobDataMap().getString("kpiName"));
                        UnscheduleJob(job.getJobDataMap().getString("kpiName"));
                    }
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }

        // Finally uncshedule deleted jobs if they are not listed anymore
        for(String kpiName : scheduledJobList)
        {
            boolean unschedule = true;
            if(!kpiName.equals("observerJob")) {

                JobDetail toBeRemovedJob = null;
                for(JobDetail jdetail : jobList) {
                    if(jdetail.getKey().getName().equals(kpiName)) {
                        unschedule = false;
                    }
                }

                if(unschedule) {
                    logger.info("Unscheduling: " + "kpiJobId" + kpiName.split("_")[1]);
                    UnscheduleJob(kpiName.split("_")[1]);
                }

            }
        }
    }

    private static final Type KPI_JSON_TYPE = new TypeToken<List<KPI_JSON>>() {}.getType();

    /**
     * Create Quartz-Scheduler jobs from the job records read from a datasource
     */
    private void CreateJobs() {

        Gson gson = new Gson();
        try {
            // kpi.json as a service data storage where we get KPI job data to be scheduled
            JsonReader reader = new JsonReader(new FileReader("kpi.json"));
            List<KPI_JSON> kpiList = gson.fromJson(reader, KPI_JSON_TYPE);

            for (KPI_JSON kpiItem : kpiList) {
                logger.info("Found KPI in kpi.json: {} , enabled: {}", kpiItem.getName(), kpiItem.getIsRunning());

                JobDetail job = newJob(KPIJSONJob.class)
                        .withIdentity("kpiJobName_" + kpiItem.getName(), "kpigroup")
                        .usingJobData("kpiName", kpiItem.getName())
                        .usingJobData("cron", kpiItem.getCron())
                        .usingJobData("lastRan", kpiItem.getLastRan())
                        .usingJobData("kpiDescription", kpiItem.getKpiDescription())
                        .usingJobData("lastMeasuredValue", kpiItem.getLastMeasuredValue())
                        .usingJobData("filename", kpiItem.getFilename())
                        .usingJobData("type", kpiItem.getType())
                        .usingJobData("isRunning", kpiItem.getIsRunning())
                        .build();

                jobList.add(job);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Schedule a job
     * @param job
     * @throws SchedulerException
     */
    private void ScheduleJob(JobDetail job) throws SchedulerException {
        String cron = job.getJobDataMap().getString("cron");
        CronTrigger trigger = newTrigger()
                .withIdentity(job.getKey().getName() + "_trigger", "kpigroup")
                .withSchedule(cronSchedule(cron))
                .startNow()
                .build();
        Date ft = kpiScheduler.scheduleJob(job, trigger);
    }

    /**
     * Unschedule a job
     * @param kpiName
     */
    private void UnscheduleJob(String kpiName)
    {
        TriggerKey tk = new TriggerKey("kpiJobName_" + kpiName + "_trigger", "kpigroup");
        try {
            kpiScheduler.unscheduleJob(tk);
            kpiScheduler.deleteJob(new JobKey("kpiJobName_" + kpiName, "kpigroup"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}

