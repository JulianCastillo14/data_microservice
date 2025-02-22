package com.smartuis.module.persistence.repository;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.smartuis.module.domian.entity.Data;
import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.domian.entity.Metric;
import com.smartuis.module.domian.repository.MessageRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public class InfluxRepository implements MessageRepository {

    private InfluxDBClient influxDBClient;

    public InfluxRepository(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }


    @Override
    public Message write(Message message) {
        List<Metric> metrics = message.getMetrics();
        List<Data> data = metrics.stream().map(metric ->
                        new  Data(message.getHeader().getLocation(),
                        metric.getMeasurement(),
                        metric.getValue())).toList();


        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        data.forEach(point->writeApi.writeMeasurement(WritePrecision.NS, point));

        return message;
    }
}