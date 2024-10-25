package de.cranix.dao;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name="RRules")
public class RRule {
    protected static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            columnDefinition = "BIGINT UNSIGNED NOT NULL AUTO_INCREMENT"
    )
    protected Long id;

    /*
     * YEARLY = 0, MONTHLY = 1, WEEKLY = 2, DAILY = 3, HOURLY = 4, MINUTELY = 5, SECONDLY = 6
     * */
    @Column(name = "freq")
    protected Integer freq = 2;

    @Column(
            name = "until",
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    protected Date until = new Date();

    @Column(name = "count")
    protected Integer count = 10;

    @Convert(converter = StringToIntegerArrayConverter.class)
    @Column(name = "interval")
    protected List<Integer> interval = new ArrayList<>();

    @Convert(converter = StringToIntegerArrayConverter.class)
    @Column(name = "byweekday")
    protected List<Integer> byweekday = new ArrayList<>();

    @Convert(converter = StringToIntegerArrayConverter.class)
    @Column(name = "bymonth")
    protected List<Integer> bymonth = new ArrayList<>();

    @Convert(converter = StringToIntegerArrayConverter.class)
    @Column(name = "bysetpos")
    protected List<Integer> bysetpos = new ArrayList<>();

    @Convert(converter = StringToIntegerArrayConverter.class)
    @Column(name = "bymonthday")
    protected List<Integer> bymonthday = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Integer> getInterval() {
        return interval;
    }

    public void setInterval(List<Integer> interval) {
        this.interval = interval;
    }

    public List<Integer> getByweekday() {
        return byweekday;
    }

    public void setByweekday(List<Integer> byweekday) {
        this.byweekday = byweekday;
    }

    public List<Integer> getBymonth() {
        return bymonth;
    }

    public void setBymonth(List<Integer> bymonth) {
        this.bymonth = bymonth;
    }

    public List<Integer> getBysetpos() {
        return bysetpos;
    }

    public void setBysetpos(List<Integer> bysetpos) {
        this.bysetpos = bysetpos;
    }

    public List<Integer> getBymonthday() {
        return bymonthday;
    }

    public void setBymonthday(List<Integer> bymonthday) {
        this.bymonthday = bymonthday;
    }
}
