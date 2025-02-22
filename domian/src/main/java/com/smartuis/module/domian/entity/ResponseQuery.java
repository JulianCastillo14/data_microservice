package com.smartuis.module.domian.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ResponseQuery {
    private Instant start;
    private Instant end;
    private List<DataDTO> data;

    public ResponseQuery(Instant start, Instant end) {
        this.start = start;
        this.end = end;
        this.data = new ArrayList<>();
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public List<DataDTO> getData() {
        return data;
    }

    public void setData(List<DataDTO> data) {
        this.data = data;
    }
}
