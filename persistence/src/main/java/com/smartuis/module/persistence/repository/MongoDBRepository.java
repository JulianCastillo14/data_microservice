package com.smartuis.module.persistence.repository;

import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.domian.repository.MessageRepository;
import com.smartuis.module.persistence.Exceptions.UnitsTimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class MongoDBRepository implements MessageRepository {

    private IMongoRepository imongoRepository;

    public MongoDBRepository(IMongoRepository imongoRepository) {
        this.imongoRepository = imongoRepository;
    }


    @Override
    public Message write(Message message) {
        return imongoRepository.save(message);
    }

    @Override
    public List<Message> findMessagesByDeviceId(String deviceId) {
        return imongoRepository.findMessagesByDeviceId(deviceId);
    }

    @Override
    public List<Message> findMessagesByLocation(String location) {
        return imongoRepository.findMessagesByLocation(location);
    }

    @Override
    public List<Message> findMessagesBetweenTwoDate(Instant from, Instant to) {
        return imongoRepository.findMessagesBetweenTwoDate(from, to);
    }

    @Override
    public List<Message> findMessagesInUnitsTime(String time) {
        var pattern = Pattern.compile("^(\\d+)(m|s|h)$");
        var matcher = pattern.matcher(time);
        System.out.println(time);
        System.out.println(matcher.matches());
        if(!matcher.matches()){
            throw new UnitsTimeException("El patron de unidad de tiempo de ser ^(\\d+)(m|s|h)$\n");
        }

        var number = Integer.parseInt(matcher.group(1));
        var unit = matcher.group(2).toLowerCase();

        var nowDate = Instant.now();
        Instant fromDate;
        System.out.println(time);
        System.out.println(nowDate);

        switch (unit){
            case "s":
                fromDate =  nowDate.minus(Duration.ofSeconds(number));
                break;
            case "m":
                fromDate =  nowDate.minus(Duration.ofMinutes(number));
                break;
            case "h":
                fromDate =  nowDate.minus(Duration.ofHours(number));
                break;
            default:
                throw new UnitsTimeException("No existe esa unidad de tiempo");
        };


        return imongoRepository.findMessagesBetweenTwoDate(fromDate, nowDate);
    }

    @Override
    public List<Message> findMessagesForMetric(String metric, Integer limit) {
        return imongoRepository.findMessagesForMetric(metric, limit);
    }


}
