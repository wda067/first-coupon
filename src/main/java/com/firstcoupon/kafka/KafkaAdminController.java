package com.firstcoupon.kafka;

import com.firstcoupon.dto.PartitionInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka/admin")
@RequiredArgsConstructor
public class KafkaAdminController {

    private final KafkaAdminClient kafkaAdminClient;

    //Kafka 토픽 목록 조회
    @GetMapping("/topics")
    public ResponseEntity<Set<String>> listTopics() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminClient.listTopics());
    }

    //특정 토픽 정보 조회
    @GetMapping("/topics/{topicName}")
    public ResponseEntity<List<PartitionInfo>> getTopicInfo(@PathVariable String topicName)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminClient.getTopicInfo(topicName));
    }

    //새로운 토픽 생성
    @PostMapping("/topics")
    public ResponseEntity<String> createTopic(@RequestParam String topicName,
                                              @RequestParam int partitions,
                                              @RequestParam short replicationFactor) {
        kafkaAdminClient.createTopic(topicName, partitions, replicationFactor);
        return ResponseEntity.ok("토픽 생성 완료: " + topicName);
    }

    //토픽의 파티션 개수 늘리기
    @PutMapping("/topics/{topicName}/partitions")
    public ResponseEntity<String> increasePartitions(@PathVariable String topicName,
                                                     @RequestParam int newPartitionCount) {
        kafkaAdminClient.increasePartitions(topicName, newPartitionCount);
        return ResponseEntity.ok("토픽 파티션 증가 완료: " + topicName);
    }

    //컨슈머 그룹 목록 조회
    @GetMapping("/consumer-groups")
    public ResponseEntity<Set<String>> listConsumerGroups() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminClient.listConsumerGroups());
    }

    //특정 컨슈머 그룹의 오프셋 정보 조회
    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<Map<TopicPartition, OffsetAndMetadata>> getConsumerGroupOffsets(@PathVariable String groupId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(kafkaAdminClient.getConsumerGroupOffsets(groupId));
    }

    //컨슈머 그룹 오프셋 초기화 (Earliest로 리셋)
    @PutMapping("/consumer-groups/{groupId}/reset")
    public ResponseEntity<String> resetConsumerGroup(@PathVariable String groupId) {
        kafkaAdminClient.resetConsumerGroupOffsets(groupId);
        return ResponseEntity.ok("컨슈머 그룹 오프셋 초기화 완료: " + groupId);
    }
}
