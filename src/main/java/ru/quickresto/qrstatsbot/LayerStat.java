package ru.quickresto.qrstatsbot;

import java.math.BigDecimal;

public class LayerStat {

    private String profile;

    private Integer diff;

    private Integer trial;

    private BigDecimal percent;

    private Integer current;

    public LayerStat(String profile, Integer diff, Integer trial, BigDecimal percent, Integer current) {
        this.profile = profile;
        this.diff = diff;
        this.trial = trial;
        this.percent = percent;
        this.current = current;
    }

    public String getProfile() {
        return profile;
    }

    public Integer getDiff() {
        return diff;
    }

    public Integer getTrial() {
        return trial;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public Integer getCurrent() {
        return current;
    }
}
