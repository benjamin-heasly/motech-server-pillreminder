package org.motechproject.scheduletracking.api.domain;

import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;
import org.motechproject.scheduletracking.api.domain.json.AlertRecord;
import org.motechproject.scheduletracking.api.domain.json.MilestoneRecord;
import org.motechproject.scheduletracking.api.domain.json.ScheduleRecord;
import org.motechproject.scheduletracking.api.domain.json.ScheduleWindowsRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduleFactory {

    private PeriodParser yearParser;
    private PeriodParser monthParser;
    private PeriodParser weekParser;
    private PeriodParser dayParser;
    private PeriodParser hourParser;

    public ScheduleFactory() {
        initializePeriodParsers();
    }

    public Schedule build(ScheduleRecord scheduleRecord) {
        Schedule schedule = new Schedule(scheduleRecord.name());
        int alertIndex = 0;
        for (MilestoneRecord milestoneRecord : scheduleRecord.milestoneRecords()) {
            ScheduleWindowsRecord windowsRecord = milestoneRecord.scheduleWindowsRecord();

            List<String> earliestValue = windowsRecord.earliest();
            List<String> dueValue = windowsRecord.due();
            List<String> lateValue = windowsRecord.late();
            List<String> maxValue = windowsRecord.max();

            Period earliest = new Period(), due = new Period(), late = new Period(), max = new Period();
            if (isWindowNotEmpty(earliestValue))
                earliest = getWindowPeriod(earliestValue);
            if (isWindowNotEmpty(dueValue))
                due = getWindowPeriod(dueValue).minus(earliest);
            if (isWindowNotEmpty(lateValue))
                late = getWindowPeriod(lateValue).minus(earliest.plus(due));
            if (isWindowNotEmpty(maxValue))
                max = getWindowPeriod(maxValue).minus(earliest.plus(due).plus(late));

            Milestone milestone = new Milestone(milestoneRecord.name(), earliest, due, late, max);
            milestone.setData(milestoneRecord.data());
            for (AlertRecord alertRecord : milestoneRecord.alerts()) {
                List<String> offset = alertRecord.offset();
                milestone.addAlert(WindowName.valueOf(alertRecord.window()), new Alert(getWindowPeriod(offset), getWindowPeriod(alertRecord.interval()), Integer.parseInt(alertRecord.count()), alertIndex++));
            }
            schedule.addMilestones(milestone);
        }
        return schedule;
    }

    private boolean isWindowNotEmpty(List<String> windowValue) {
        return !getWindowPeriod(windowValue).equals(new Period());
    }

    private void initializePeriodParsers() {
        yearParser = new PeriodFormatterBuilder()
                .appendYears()
                .appendSuffix(" year", " years")
                .toParser();
        monthParser = new PeriodFormatterBuilder()
                .appendMonths()
                .appendSuffix(" month", " months")
                .toParser();
        weekParser = new PeriodFormatterBuilder()
                .appendWeeks()
                .appendSuffix(" week", " weeks")
                .toParser();
        dayParser = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix(" day", " days")
                .toParser();
        hourParser = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" hour", " hours")
                .toParser();
    }

    private Period getWindowPeriod(List<String> readableValues) {
        ReadWritablePeriod period = new MutablePeriod();
        for (String s : readableValues)
            period.add(parse(s));
        return period.toPeriod();
    }

    private Period parse(String s) {
        ReadWritablePeriod period = new MutablePeriod();
        if (yearParser.parseInto(period, s, 0, null) > 0)
            return period.toPeriod();
        if (monthParser.parseInto(period, s, 0, null) > 0)
            return period.toPeriod();
        if (weekParser.parseInto(period, s, 0, null) > 0)
            return period.toPeriod();
        if (dayParser.parseInto(period, s, 0, null) > 0)
            return period.toPeriod();
        if (hourParser.parseInto(period, s, 0, null) > 0)
            return period.toPeriod();
        return period.toPeriod();
    }
}
