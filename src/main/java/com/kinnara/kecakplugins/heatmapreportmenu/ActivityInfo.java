package com.kinnara.kecakplugins.heatmapreportmenu;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by akbar on 7/18/2017.
 */
public class ActivityInfo {

    private String  activityId              = "";
    private Integer activityHitCount        = 0;
    private Long    activityLeadTime        = 0l;
    private Double  activityAverageHitCount = 0.0;
    private Double  activityAverageLeadTime = 0.0;

    public ActivityInfo() {
    }

    @Override
    public String toString() {
        return String.format(
         "Activity ID           : %s\n" +
          "Average Hit Count     : %s\n" +
          "Average Lead Time     : %s\n",
         activityId,
         activityHitCount,
         String.valueOf(activityLeadTime)
        );
    }

    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("activityId", getActivityId());
        map.put("activityAverageHitCount", getActivityAverageHitCount());
        map.put("activityAverageLeadTime", getActivityAverageLeadTime());

        return map;
    }

    //<editor-fold desc="Getter" defaultstate="collapsed">
    public String getActivityId() {
        return activityId;
    }

    public Integer getActivityHitCount() {
        return activityHitCount;
    }

    public Long getActivityLeadTime() {
        return activityLeadTime;
    }

    public Double getActivityAverageHitCount() {
        return activityAverageHitCount;
    }

    public Double getActivityAverageLeadTime() {
        return activityAverageLeadTime;
    }
    //</editor-fold>

    //<editor-fold desc="Setter" defaultstate="collapsed">
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public void setActivityHitCount(Integer activityHitCount) {
        this.activityHitCount = activityHitCount;
    }

    public void setActivityLeadTime(Long activityLeadTime) {
        this.activityLeadTime += activityLeadTime;
    }

    public void setActivityAverageHitCount(Double activityAverageHitCount) {
        this.activityAverageHitCount = activityAverageHitCount;
    }

    public void setActivityAverageLeadTime(Double activityAverageLeadTime) {
        this.activityAverageLeadTime = activityAverageLeadTime;
    }
    //</editor-fold>
}
