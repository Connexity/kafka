/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.test;


import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.record.TimestampType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MockConsumerInterceptor implements ConsumerInterceptor<String, String> {
    public static final AtomicInteger INIT_COUNT = new AtomicInteger(0);
    public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);
    public static final AtomicInteger ON_COMMIT_COUNT = new AtomicInteger(0);

    public MockConsumerInterceptor() {
        INIT_COUNT.incrementAndGet();
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // clientId must be in configs
        Object clientIdValue = configs.get(ConsumerConfig.CLIENT_ID_CONFIG);
        if (clientIdValue == null)
            throw new ConfigException("Mock consumer interceptor expects configuration " + ProducerConfig.CLIENT_ID_CONFIG);
    }

    @Override
    public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> records) {
        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordMap = new HashMap<>();
        for (TopicPartition tp : records.partitions()) {
            List<ConsumerRecord<String, String>> lst = new ArrayList<>();
            for (ConsumerRecord<String, String> record: records.records(tp)) {
                lst.add(new ConsumerRecord<>(record.topic(), record.partition(), record.offset(),
                    0L, TimestampType.CREATE_TIME, record.key(), record.value().toUpperCase()));
            }
            recordMap.put(tp, lst);
        }
        return new ConsumerRecords<String, String>(recordMap);
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        ON_COMMIT_COUNT.incrementAndGet();
    }

    @Override
    public void close() {
        CLOSE_COUNT.incrementAndGet();
    }

    public static void resetCounters() {
        INIT_COUNT.set(0);
        CLOSE_COUNT.set(0);
        ON_COMMIT_COUNT.set(0);
    }
}
