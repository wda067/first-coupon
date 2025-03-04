package com.firstcoupon.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.kafka.common.Node;

@Getter
public class PartitionInfo {

    private int partition;
    private String leader;
    private List<String> replicas;

    public PartitionInfo(int partition, Node leader, List<Node> replicas) {
        this.partition = partition;
        this.leader = leader.host() + ":" + leader.port() + " (id: " + leader.id() + " rack: "
                + leader.rack() + ")";
        this.replicas = replicas.stream()
                .map(node -> leader.host() + ":" + leader.port() + " (id: " + leader.id() + " rack: " + leader.rack()
                        + ")")
                .collect(Collectors.toList());
    }

}
