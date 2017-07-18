package com.kinnara.kecakplugins.heatmapreportmenu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by akbar on 7/18/2017.
 */
public class ActivityInfo {

    private String               activityId          = "";
    private String               activityName        = "";
    private String               activityDateCreated = "";
    private Integer              activityHitCount    = 0;
    private Long                 activityDue         = 0l;
    private Long                 activityLeadTime    = 0l;
    private Map<String, Integer> activityUser        = new TreeMap<>();

    public ActivityInfo() {
    }

    @Override
    public String toString() {
        return String.format(
         "Activity ID   : %s\n" +
          "Activity ActivityName : %s\n" +
          "Hit ActivityHitCount     : %s\n" +
          "ActivityLeadTime      : %s\n" +
          "ActivityDue           : %s\n" +
          "Date Created  : %s\n" +
          "ActivityUser          : %s\n",
         activityId,
         activityName,
         activityHitCount,
         String.valueOf(activityLeadTime),
         String.valueOf(activityDue),
         String.valueOf(activityDateCreated),
         getActivityUser()
        );
    }

    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("activityId", getActivityId());
        map.put("activityName", getActivityId());
        map.put("activityDateCreated", getActivityDateCreated());
        map.put("activityHitCount", getActivityHitCount());
        map.put("activityDue", getActivityDue());
        map.put("activityLeadTime", getActivityLeadTime());
        map.put("activityUser", getActivityUser());

        return map;
    }

    //<editor-fold desc="Getter" defaultstate="collapsed">
    public String getActivityId() {
        return activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityDateCreated() {
        return activityDateCreated;
    }

    public Integer getActivityHitCount() {
        return activityHitCount;
    }

    public Long getActivityDue() {
        return activityDue;
    }

    public Long getActivityLeadTime() {
        return activityLeadTime;
    }

    public List<String> getActivityUser() {
        List<String> listActivityUser = new ArrayList<>();

        for (String each : activityUser.keySet()) {
            listActivityUser.add(each);
        }

        return listActivityUser;
    }
    //</editor-fold>

    //<editor-fold desc="Setter" defaultstate="collapsed">
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setActivityDateCreated(String activityDateCreated) {
        this.activityDateCreated = activityDateCreated;
    }

    public void setActivityHitCount(Integer activityHitCount) {
        this.activityHitCount = activityHitCount;
    }

    public void setActivityDue(Long activityDue) {
        this.activityDue += activityDue;
    }

    public void setActivityLeadTime(Long activityLeadTime) {
        this.activityLeadTime += activityLeadTime;
    }

    public void setActivityUser(String[] activityUsers) {
        for (String each : activityUsers) {
            this.activityUser.put(each, 0);
        }
    }

    //</editor-fold>
}
