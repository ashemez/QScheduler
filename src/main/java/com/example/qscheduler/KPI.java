package com.example.qscheduler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KPI {
    public KPI() {}

    private String name;
    private String type;
    private String cron;
    private int isRunning;
    private String lastRan;
}
