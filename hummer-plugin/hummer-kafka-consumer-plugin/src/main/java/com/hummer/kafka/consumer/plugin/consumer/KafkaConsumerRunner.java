package com.hummer.kafka.consumer.plugin.consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.hummer.kafka.consumer.plugin.ConsumerProperties;
import com.hummer.kafka.consumer.plugin.handle.MessageBodyMetadata;
import joptsimple.internal.Strings;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * kafka consumer runner
 *
 * @Author: lee
 * @since:1.0.0
 * @Date: 2019/8/12 16:31
 **/
public class KafkaConsumerRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRunner.class);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final KafkaConsumer<String, Object> consumer;
    private final ConsumerMetadata metadata;

    public KafkaConsumerRunner(final ConsumerMetadata metadata) {
        Preconditions.checkArgument(Strings.isNullOrEmpty(metadata.getGroupName())
                , "please settings consumer group name");
        this.metadata = metadata;
        this.consumer = new KafkaConsumer<>(ConsumerProperties.builderProperties(metadata.getGroupName()));
        this.consumer.subscribe(Collections.singleton(metadata.getTopicId()), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                LOGGER.info("kafka consumer lost partition in reblance");
                for (TopicPartition partition : partitions) {
                    consumer.seek(partition, 0);
                }
            }
        });
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            while (!closed.get()) {
                ConsumerRecords<String, Object>
                        records = consumer.poll(Duration.of(metadata.getPollTimeOutMillis()
                        , ChronoUnit.MILLIS));
                Iterator<ConsumerRecord<String, Object>> recordIterator = records.iterator();
                List<MessageBodyMetadata> list = Lists.newArrayListWithCapacity(16);
                while (recordIterator.hasNext()) {
                    ConsumerRecord<String, Object> recordItem = recordIterator.next();
                    if (recordItem != null) {
                        list.add(MessageBodyMetadata
                                .builder()
                                .body(recordItem.value())
                                .key(recordItem.key())
                                .build());
                    }
                }
                LOGGER.debug("consumer will handle {} count message", list.size());
                long start = System.currentTimeMillis();
                try {
                    metadata.getHandleBusiness().handle(ImmutableList.copyOf(list));
                } catch (Throwable throwable) {
                    LOGGER.error("business handle failed ", throwable);
                }
                LOGGER.debug("business handle done cost {} millis", System.currentTimeMillis() - start);
                consumer.commitAsync(new CommitOffsetCallback());
            }
        } catch (WakeupException e) {
            //ignore
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
        LOGGER.info("kafka consumer shutdown");
    }
}
