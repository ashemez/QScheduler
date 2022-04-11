package com.example.qscheduler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KPI_JSON extends KPI {
    public KPI_JSON() {}

    private String kpiDescription;
    private String lastMeasuredValue;
    private String filename;
}
