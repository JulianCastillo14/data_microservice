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

    public InfluxRepository(InfluxDBClient influxDBClient, InfluxDBConfig influxDBConfig) {
        this.influxDBClient = influxDBClient;
        this.bucket = influxDBConfig.getBucket();
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
    public List<Message> findLastMeasurements(String measurement, int limit) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: 0) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\") " +
                        "|> sort(columns:[\"_time\"], desc: true) " +
                        "|> limit(n: %d)",
                bucket , measurement, limit);

        List<FluxTable> tables = queryData(flux);
        List<Message> messages = transformMessagesToResponse(tables);

        return messages;
    }

    @Override
    public List<Message> findMeasurementsByTimeRange(String measurement, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\")",
                bucket, start.toString(), end.toString(), measurement);

        List<FluxTable> tables = queryData(flux);
        List<Message> messages = transformMessagesToResponse(tables);

        return messages;
    }

    @Override
    public List<Message> findMessagesBetweenTwoDate(Instant from, Instant to) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> sort(columns:[\"_time\"])",
                bucket, from.toString(), to.toString()
        );
        List<FluxTable> tables = queryData(flux);
        List<Message> messages = transformMessagesToResponse(tables);
        return messages;
    }

    @Override
    public List<Message> findMessagesInUnitsTime(String time) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: -%s) |> sort(columns:[\"_time\"])",
                bucket, time
        );
        List<FluxTable> tables = queryData(flux);
        List<Message> messages = transformMessagesToResponse(tables);
        return messages;
    }



    @Override
    public Optional<Double> findAverageValue(String measurement, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> mean()",
                bucket, start.toString(), end.toString(), measurement);
        List<FluxTable> tables = queryData(flux);
        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            Object value = tables.get(0).getRecords().get(0).getValue();
            if (value instanceof Number) {
                return Optional.of(((Number) value).doubleValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> findMaxValue(String measurement, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> max()",
                bucket, start.toString(), end.toString(), measurement);

        List<FluxTable> tables = queryData(flux);

        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            Object value = tables.get(0).getRecords().get(0).getValue();
            if (value instanceof Number) {
                return Optional.of(((Number) value).doubleValue());
            }
        }

        return Optional.empty();
    }


    @Override
    public Optional<Double> findMinValue(String measurement, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> min()",
                bucket, start.toString(), end.toString(), measurement);
        List<FluxTable> tables = queryData(flux);

        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            Object value = tables.get(0).getRecords().get(0).getValue();
            if (value instanceof Number) {
                return Optional.of(((Number) value).doubleValue());
            }
        }

        return Optional.empty();
    }

    public List<FluxTable> queryData(String query) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(query);
    }

    public List<Message> transformMessagesToResponse(List<FluxTable> tables) {
        List<Message> messages = new ArrayList<>();

        for (FluxTable table : tables) {
            for(FluxRecord  record : table.getRecords()){
                String location = (String) record.getValues().get("location");
                String _measurement = (String) record.getValues().get("_measurement");
                Double value = (Double) record.getValues().get("_value");
                Instant time = (Instant) record.getValues().get("_time");

                Header header = new Header(null, null, location);
                header.setTimeStamp(time);

                Metric metric = new Metric(_measurement, value);

                messages.add(new Message(header, List.of(metric)));
            }
        }
        return messages;
    }


}