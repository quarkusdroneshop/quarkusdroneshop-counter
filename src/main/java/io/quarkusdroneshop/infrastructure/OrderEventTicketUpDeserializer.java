package io.quarkusdroneshop.infrastructure;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * dataproduct-order-events (Avro, order-events Flink job が発行) から
 * LINE_ITEM_STATUS_CHANGED イベントのみを TicketUp に変換する。
 * それ以外の eventType (ORDER_PLACED / ORDER_CANCELLED) は null を返し、
 * KafkaService#orderUp 側の null チェックでスキップされる。
 */
public class OrderEventTicketUpDeserializer implements Deserializer<TicketUp> {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventTicketUpDeserializer.class);

    private final AvroKafkaDeserializer<GenericRecord> avroDeserializer = new AvroKafkaDeserializer<>();

    @Override
    public void configure(java.util.Map<String, ?> configs, boolean isKey) {
        avroDeserializer.configure(configs, isKey);
    }

    @Override
    public TicketUp deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        GenericRecord record = avroDeserializer.deserialize(topic, new RecordHeaders(), data);
        if (record == null) {
            return null;
        }

        Object eventTypeObj = record.get("eventType");
        if (eventTypeObj == null || !"LINE_ITEM_STATUS_CHANGED".equals(eventTypeObj.toString())) {
            return null;
        }

        GenericRecord lineItem = (GenericRecord) record.get("lineItem");
        if (lineItem == null) {
            logger.warn("LINE_ITEM_STATUS_CHANGED event without lineItem: {}", record);
            return null;
        }

        try {
            UUID orderId = UUID.fromString(record.get("orderId").toString());
            UUID lineItemId = UUID.fromString(lineItem.get("itemId").toString());
            Item item = Item.valueOf(lineItem.get("item").toString());
            String name = String.valueOf(lineItem.get("name"));
            OrderStatus status = OrderStatus.valueOf(lineItem.get("lineItemStatus").toString());
            Instant timestamp = Instant.ofEpochMilli((Long) record.get("eventTimestamp"));
            Object madeByObj = lineItem.get("madeBy");
            String madeBy = madeByObj != null ? madeByObj.toString() : null;

            return new TicketUp(orderId, lineItemId, item, name, timestamp, status, madeBy);
        } catch (Exception e) {
            logger.warn("Failed to convert OrderEvent to TicketUp: {}", record, e);
            return null;
        }
    }

    @Override
    public void close() {
        avroDeserializer.close();
    }
}
