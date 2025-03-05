package com.smartuis.module.persistence.repository;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.smartuis.module.domian.entity.Data;
import com.smartuis.module.domian.entity.Header;
import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.domian.entity.Metric;
import com.smartuis.module.domian.repository.MessageRepository;
import com.smartuis.module.domian.repository.StatisticsQuery;
import com.smartuis.module.domian.repository.TemporaryQuery;
import com.smartuis.module.persistence.config.InfluxDBConfig;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InfluxRepository implements MessageRepository, TemporaryQuery, StatisticsQuery {

    private InfluxDBClient influxDBClient;
    private String bucket;
    private String org;

    public InfluxRepository(InfluxDBClient influxDBClient, InfluxDBConfig influxDBConfig) {
        this.influxDBClient = influxDBClient;
        this.bucket = influxDBConfig.getBucket();
        this.org = influxDBConfig.getOrg();
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


    @Override
    public List<Message> findMessagesBetweenTwoDate(Instant from, Instant to) {
        return List.of();
    }

    @Override
    public List<Message> findMessagesInUnitsTime(String time) {
        return List.of();
    }


    @Override
    public List<Message> findLastMeasurements(String measurement, int limit) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: 0) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\") " +
                        "|> sort(columns:[\"_time\"], desc: true) " +
                        "|> limit(n: %d)",
                bucket , measurement, limit);

        return queryAndMapToMessages(flux);
    }

    @Override
    public List<Message> findMeasurementsByTimeRange(String measurement, Instant start, Instant end) {
        return List.of();
    }

    @Override
    public Optional<Double> findAverageValue(String measurement, Instant start, Instant end) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> findMaxValue(String measurement, Instant start, Instant end) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> findMinValue(String measurement, Instant start, Instant end) {
        return Optional.empty();
    }

    private List<Message> queryAndMapToMessages(String flux) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, org);
        List<Message> messages = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Message msg = mapRecordToMessage(record);
                messages.add(msg);
            }
        }
        return messages;
    }

    private Message mapRecordToMessage(FluxRecord record) {
        Message message = new Message();

        Header header = new Header();

        System.out.println(record.getField());

        header.setUserUUID((String) record.getValueByKey("userUUID"));
        header.setDeviceId((String) record.getValueByKey("deviceId"));
        header.setLocation((String) record.getValueByKey("location"));
        header.setTimeStamp(Instant.parse(record.getTime().toString()));
        message.setHeader(header);

        Metric metric = new Metric();
        metric.setMeasurement(record.getMeasurement());
        Object value = record.getValue();
        if (value instanceof Number) {
            metric.setValue(((Number) value).doubleValue());
        }
        List<Metric> metrics = new ArrayList<>();
        metrics.add(metric);
        message.setMetrics(metrics);

        return message;
    }
}