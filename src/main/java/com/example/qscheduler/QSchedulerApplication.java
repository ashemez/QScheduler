package com.example.qscheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class QSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(QSchedulerApplication.class, args);
	}

	Scheduler kpiScheduler;

	@EventListener(ApplicationReadyEvent.class)
	public void onAppStartUp() {
		try {
			// initializing KPI Trigger
			SchedulerFactory sf = new StdSchedulerFactory();
			kpiScheduler = sf.getScheduler();

			// watcher runs an observer job which monitors and manages KPI jobs
			KPIJobWatcher watcher = new KPIJobWatcher(kpiScheduler);
			watcher.run();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
