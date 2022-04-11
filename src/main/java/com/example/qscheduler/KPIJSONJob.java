package com.example.qscheduler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;

public class KPIJSONJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(KPIJSONJob.class);

    private KPI_JSON kpi;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        kpi.setName(dataMap.getString("kpiName"));
        kpi.setKpiDescription(dataMap.getString("kpiDescription"));
        kpi.setIsRunning(dataMap.getInt("isRunning"));
        kpi.setFilename(dataMap.getString("filename"));
        kpi.setCron(dataMap.getString("cron"));
        kpi.setLastRan(dataMap.getString("lastRan"));
        kpi.setType(dataMap.getString("type"));
        kpi.setLastMeasuredValue(dataMap.getString("lastMeasuredValue"));

        this.processKPI();
    }

    public class KPIMeasured {
        public String name;
        public String value;
    }

    private static final Type KPIMEASURED_TYPE = new TypeToken<List<KPIMeasured>>() {}.getType();
    protected void processKPI() {
        // processKPI is supposed to get the KPI measured value from an external datasource and updates kpi.json
        // ...
    }

}

