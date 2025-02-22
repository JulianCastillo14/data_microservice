package com.smartuis.module.domian.entity;

import java.util.List;

public class Message {
    private Header headers;
    private List<Metric> metrics;

    public Message(Header headers, List<Metric> metrics) {
        this.headers = headers;
        this.metrics = metrics;
    }

    public Header getHeader() {
        return headers;
    }

    public void setHeader(Header headers) {
        this.headers = headers;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        return "Message{" +
                "headers=" + headers +
                ", metrics=" + metrics +
                '}';
    }
}
