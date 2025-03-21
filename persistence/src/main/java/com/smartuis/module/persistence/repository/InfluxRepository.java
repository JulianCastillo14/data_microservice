package com.smartuis.module.persistence.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxTable;
import com.module.service.impl.AmqpRequeueService;
import com.module.service.impl.InfluxService;
import com.module.service.impl.MqttRequeueService;
import com.smartuis.module.domian.entity.*;
import com.smartuis.module.domian.repository.MessageRepository;
import com.smartuis.module.domian.repository.StatisticsQuery;
import com.smartuis.module.domian.repository.TemporaryQuery;
import com.smartuis.module.persistence.config.InfluxDBConfig;
import com.smartuis.module.persistence.mapper.FluxRecordMapper;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class InfluxRepository implements MessageRepository, TemporaryQuery, StatisticsQuery {

    private InfluxDBClient influxDBClient;
    private String bucket;
    private FluxRecordMapper fluxRecordMapper;
    private InfluxService influxService;
    private MqttRequeueService mqttRequeueService;
    private AmqpRequeueService  amqpRequeueService;
    private DeviceRepository deviceRepository;

    public InfluxRepository(InfluxDBClient influxDBClient, InfluxDBConfig influxDBConfig, InfluxService influxService, MqttRequeueService mqttRequeueService, AmqpRequeueService amqpRequeueService, DeviceRepository deviceRepository) {
        this.influxDBClient = influxDBClient;
        this.bucket = influxDBConfig.getBucket();
        this.fluxRecordMapper = new FluxRecordMapper();
        this.influxService = influxService;
        this.mqttRequeueService = mqttRequeueService;
        this.amqpRequeueService = amqpRequeueService;
        this.deviceRepository =  deviceRepository;
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

        if (message.getHeader().getShouldRequeue()) {
            String deviceId = message.getHeader().getDeviceId();
            Optional<Device> deviceOpt = deviceRepository.findDeviceByDeviceId(deviceId);
            List<Application> applications = deviceOpt.get().getApplications();

            for(Application application : applications){
                if(message.getHeader().getTopic().equals(application.getName())){
                    Header header = (Header) message.getHeader().clone();
                    Message  messageRequeue = new Message();
                    messageRequeue.setHeader(header);
                    messageRequeue.setMetrics(message.getMetrics());

                    String newTopic = messageRequeue.getHeader().getTopic() + "/" + application.getApplicationId();
                    messageRequeue.getHeader().setTopic(newTopic);
                    mqttRequeueService.requeue(messageRequeue);
                    amqpRequeueService.requeue(messageRequeue);
                }
            }

        }
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

        List<FluxTable> tables = influxService.queryData(flux);

        return fluxRecordMapper.mapFluxTablesToMessages(tables);
    }

    @Override
    public List<Message> findMeasurementsByTimeRange(String measurement, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\")",
                bucket, start.toString(), end.toString(), measurement);

        List<FluxTable> tables = influxService.queryData(flux);
        return fluxRecordMapper.mapFluxTablesToMessages(tables);
    }

    @Override
    public List<Message> findMessagesBetweenTwoDate(Instant from, Instant to) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> sort(columns:[\"_time\"])",
                bucket, from.toString(), to.toString()
        );
        List<FluxTable> tables = influxService.queryData(flux);
        return fluxRecordMapper.mapFluxTablesToMessages(tables);
    }

    @Override
    public List<Message> findMessagesInUnitsTime(String time) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: -%s) |> sort(columns:[\"_time\"])",
                bucket, time
        );
        List<FluxTable> tables = influxService.queryData(flux);
        return fluxRecordMapper.mapFluxTablesToMessages(tables);
    }


    @Override
    public Optional<Double> findAverageValue(String measurement, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"%s\") |> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"%s\" and r._field == \"value\") " +
                        "|> mean()",
                bucket, start.toString(), end.toString(), measurement);
        List<FluxTable> tables = influxService.queryData(flux);
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

        List<FluxTable> tables = influxService.queryData(flux);

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
        List<FluxTable> tables = influxService.queryData(flux);

        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            Object value = tables.get(0).getRecords().get(0).getValue();
            if (value instanceof Number) {
                return Optional.of(((Number) value).doubleValue());
            }
        }
        return Optional.empty();
    }
}