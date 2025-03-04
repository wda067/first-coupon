package com.firstcoupon.kafka;

import com.firstcoupon.dto.PartitionInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAdminClient {

    private final AdminClient adminClient;

    //Kafka 토픽 목록 조회
    public Set<String> listTopics() throws ExecutionException, InterruptedException {
        return adminClient.listTopics().names().get();
    }

    //특정 토픽 정보 조회
    public List<PartitionInfo> getTopicInfo(String topicName)
            throws ExecutionException, InterruptedException {
        TopicDescription topicDescription = adminClient.describeTopics(Collections.singletonList(topicName))
                .topicNameValues()
                .get(topicName)
                .get();
        List<PartitionInfo> list = new ArrayList<>();
        for (TopicPartitionInfo partition : topicDescription.partitions()) {
            list.add(new PartitionInfo(partition.partition(), partition.leader(), partition.replicas()));
        }
        return list;
    }

    //새로운 토픽 생성
    public void createTopic(String topicName, int partitions, short replicationFactor) {
        NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
        adminClient.createTopics(Collections.singleton(newTopic));
        log.info("토픽 생성: {}", topicName);
    }

    //토픽의 파티션 개수 늘리기
    public void increasePartitions(String topicName, int newPartitionCount) {
        Map<String, NewPartitions> newPartitions = Collections.singletonMap(
                topicName, NewPartitions.increaseTo(newPartitionCount)
        );
        adminClient.createPartitions(newPartitions);
        log.info("토픽 파티션 증가: {} -> {}", topicName, newPartitionCount);
    }

    //컨슈머 그룹 목록 조회
    public Set<String> listConsumerGroups() throws ExecutionException, InterruptedException {
        return adminClient.listConsumerGroups()
                .valid()
                .get()
                .stream()
                .map(ConsumerGroupListing::groupId)
                .collect(Collectors.toSet());
    }

    //특정 컨슈머 그룹의 오프셋 정보 조회
    public Map<TopicPartition, OffsetAndMetadata> getConsumerGroupOffsets(String groupId)
            throws ExecutionException, InterruptedException {
        return adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
    }

    //컨슈머 그룹 오프셋 초기화 (Earliest로 리셋)
    public void resetConsumerGroupOffsets(String groupId) {
        try {
            //컨슈머 그룹의 현재 오프셋 가져오기
            Map<TopicPartition, OffsetAndMetadata> offsets = adminClient
                    .listConsumerGroupOffsets(groupId)
                    .partitionsToOffsetAndMetadata()
                    .get();

            //최신 오프셋을 earliest(처음)으로 리셋하기 위해 새로운 오프셋 조회
            Map<TopicPartition, OffsetAndMetadata> resetOffsets = new HashMap<>();
            try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
                consumer.assign(offsets.keySet());  //파티션 할당
                consumer.seekToBeginning(offsets.keySet());  //earliest로 이동

                //변경된 오프셋을 저장
                for (TopicPartition partition : offsets.keySet()) {
                    long newOffset = consumer.position(partition);  //초기 위치 가져오기
                    resetOffsets.put(partition, new OffsetAndMetadata(newOffset));
                }
            }

            //Kafka에 새로운 오프셋 적용 (earliest로 리셋)
            adminClient.alterConsumerGroupOffsets(groupId, resetOffsets).all().get();
            log.info("컨슈머 그룹 {} 오프셋 초기화 완료", groupId);
        } catch (Exception e) {
            log.error("컨슈머 그룹 {} 오프셋 초기화 실패", groupId, e);
            throw new RuntimeException("컨슈머 그룹 오프셋 초기화 실패", e);
        }
    }


    private KafkaConsumer<String, String> createConsumer(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props);
    }
}

