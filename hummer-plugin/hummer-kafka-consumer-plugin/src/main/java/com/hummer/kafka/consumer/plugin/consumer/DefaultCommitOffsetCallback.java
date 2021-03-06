package com.hummer.kafka.consumer.plugin.consumer;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.hummer.kafka.consumer.plugin.KafkaConsumerConstant.COMMIT_OFFSET_CALLBACK_DEFAULT;

/**
 * handle offset commit callback
 *
 * @Author: lee
 * @since:1.0.0
 * @Date: 2019/8/12 18:32
 **/
@Service(value = COMMIT_OFFSET_CALLBACK_DEFAULT)
public class DefaultCommitOffsetCallback implements OffsetCommitCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCommitOffsetCallback.class);
    @Autowired
    private OffsetStore offsetStore;

    /**
     * A callback method the user can implement to provide asynchronous handling of commit request completion.
     * This method will be called when the commit request sent to the server has been acknowledged.
     *
     * @param offsets   A map of the offsets and associated metadata that this callback applies to
     * @param exception The exception thrown during processing of the request, or null if the commit completed successfully
     * @throws CommitFailedException  if the commit failed and cannot be retried.
     *                                This can only occur if you are using automatic group management with {@link KafkaConsumer#subscribe(Collection)},
     *                                or if there is an active group with the same groupId which is using group management.
     * @throws WakeupException        if {@link KafkaConsumer#wakeup()} is called before or while this
     *                                function is called
     * @throws InterruptException     if the calling thread is interrupted before or while
     *                                this function is called
     * @throws AuthorizationException if not authorized to the topic or to the
     *                                configured groupId. See the exception for more details
     * @throws KafkaException         for any other unrecoverable errors (e.g. if offset metadata
     *                                is too large or if the committed offset is invalid).
     */
    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
        if (exception != null) {
            LOGGER.error("kafka consumer offset commit failed!,this offsets metadata is {},exception is {},"
                    , offsets, exception);
        }
        //Persistence commit failed offset
        long start = System.currentTimeMillis();
        for (Map.Entry<TopicPartition, OffsetAndMetadata> metadataEntry : offsets.entrySet()) {
            offsetStore.store(metadataEntry.getKey().topic()
                    , metadataEntry.getValue().offset()
                    , metadataEntry.getKey().hashCode());
        }

        LOGGER.debug("local persistent kafka topic done , cost {} mills"
                , System.currentTimeMillis() - start);
    }
}
