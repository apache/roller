package org.tightblog.domain;

import java.util.List;

public class CalendarData {

    private String calendarTitle;
    private String[] dayOfWeekNames;
    private String thisMonthLink;
    private String prevMonthLink;
    private String nextMonthLink;
    private String homeLink;
    private Week[] weeks = new Week[6];

    public String getCalendarTitle() {
        return calendarTitle;
    }

    public void setCalendarTitle(String calendarTitle) {
        this.calendarTitle = calendarTitle;
    }

    public String[] getDayOfWeekNames() {
        return dayOfWeekNames == null ? null : dayOfWeekNames.clone();
    }

    public void setDayOfWeekNames(String[] dayOfWeekNames) {
        this.dayOfWeekNames = dayOfWeekNames == null ? null : dayOfWeekNames.clone();
    }

    public String getThisMonthLink() {
        return thisMonthLink;
    }

    public void setThisMonthLink(String thisMonthLink) {
        this.thisMonthLink = thisMonthLink;
    }

    public String getPrevMonthLink() {
        return prevMonthLink;
    }

    public void setPrevMonthLink(String prevMonthLink) {
        this.prevMonthLink = prevMonthLink;
    }

    public String getNextMonthLink() {
        return nextMonthLink;
    }

    public void setNextMonthLink(String nextMonthLink) {
        this.nextMonthLink = nextMonthLink;
    }

    public String getHomeLink() {
        return homeLink;
    }

    public void setHomeLink(String homeLink) {
        this.homeLink = homeLink;
    }

    public Week getWeek(int weekNum) {
        return weeks[weekNum];
    }

    public void setWeek(int weekNum, Week week) {
        weeks[weekNum] = week;
    }

    public static class Week {
        private Day[] days = new Day[7];

        public Day getDay(int dayNum) {
            return days[dayNum];
        }

        public void setDay(int dayNum, Day day) {
            days[dayNum] = day;
        }
    }

    public static class Day {
        private String dayNum;
        private String link;
        private boolean isToday;
        private List<BlogEntry> entries;

        public String getDayNum() {
            return dayNum;
        }

        public void setDayNum(String dayNum) {
            this.dayNum = dayNum;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public boolean isToday() {
            return isToday;
        }

        public void setToday(boolean today) {
            isToday = today;
        }

        public List<BlogEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<BlogEntry> entries) {
            this.entries = entries;
        }
    }

    public static class BlogEntry {
        private String title;
        private String link;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
