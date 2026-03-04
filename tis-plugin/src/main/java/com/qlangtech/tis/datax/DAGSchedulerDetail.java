package com.qlangtech.tis.datax;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for DAGSchedulerActor scheduled crontab entries detail.
 *
 * @author baisui
 */
public class DAGSchedulerDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ScheduleEntryInfo> schedules = new ArrayList<>();

    public List<ScheduleEntryInfo> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleEntryInfo> schedules) {
        this.schedules = schedules;
    }

    public static class ScheduleEntryInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String pipelineName;
        private String cronExpression;
        private boolean turnOn;
        private long registerTime;
        private long lastTriggerTime;
        private long nextFireTime;

        public String getPipelineName() {
            return pipelineName;
        }

        public void setPipelineName(String pipelineName) {
            this.pipelineName = pipelineName;
        }

        public String getCronExpression() {
            return cronExpression;
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        public boolean isTurnOn() {
            return turnOn;
        }

        public void setTurnOn(boolean turnOn) {
            this.turnOn = turnOn;
        }

        public long getRegisterTime() {
            return registerTime;
        }

        public void setRegisterTime(long registerTime) {
            this.registerTime = registerTime;
        }

        public long getLastTriggerTime() {
            return lastTriggerTime;
        }

        public void setLastTriggerTime(long lastTriggerTime) {
            this.lastTriggerTime = lastTriggerTime;
        }

        public long getNextFireTime() {
            return nextFireTime;
        }

        public void setNextFireTime(long nextFireTime) {
            this.nextFireTime = nextFireTime;
        }
    }
}