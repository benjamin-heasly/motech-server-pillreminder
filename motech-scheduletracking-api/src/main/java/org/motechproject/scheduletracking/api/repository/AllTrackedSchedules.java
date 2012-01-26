package org.motechproject.scheduletracking.api.repository;

import org.motechproject.scheduletracking.api.domain.Schedule;
import org.motechproject.scheduletracking.api.domain.ScheduleFactory;
import org.motechproject.scheduletracking.api.domain.userspecified.ScheduleRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AllTrackedSchedules {
    private Map<String, Schedule> schedules = new HashMap<String, Schedule>();

    @Autowired
    public AllTrackedSchedules(TrackedSchedulesJsonReader schedulesJsonReader) {
        List<ScheduleRecord> scheduleRecords = schedulesJsonReader.records();
        for (ScheduleRecord scheduleRecord : scheduleRecords) {
            schedules.put(scheduleRecord.name(), ScheduleFactory.create(scheduleRecord));
        }
    }

    public Schedule getByName(String name) {
        return schedules.get(name);
    }
}