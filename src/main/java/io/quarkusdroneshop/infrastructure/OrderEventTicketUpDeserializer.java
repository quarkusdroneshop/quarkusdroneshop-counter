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

/**
 * dataproduct-order-events (Avro, order-events Flink job が発行) から
 * LINE_ITEM_STATUS_CHANGED / ORDER_CANCELLED イベントを TicketUp に変換する。
 * ORDER_CANCELLED は qdca10/qdca10pro の欠品(eighty-six)から生成されるイベントで、
 * status=CANCELLED の TicketUp として扱うことで、既存の orders-up 処理経路
 * (KafkaService#orderUp → OrderService#onOrderUpTx) を再利用して該当明細を
 * CANCELLED に更新できるようにする。それ以外の eventType (ORDER_PLACED) は
 * null を返し、KafkaService#orderUp 側の null チェックでスキップされる。
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
        String eventType = eventTypeObj != null ? eventTypeObj.toString() : null;
        if (!"LINE_ITEM_STATUS_CHANGED".equals(eventType) && !"ORDER_CANCELLED".equals(eventType)) {
            return null;
        }

        GenericRecord lineItem = (GenericRecord) record.get("lineItem");
        if (lineItem == null) {
            logger.warn("{} event without lineItem: {}", eventType, record);
            return null;
        }

        try {
            String orderId = record.get("orderId").toString();
            String lineItemId = lineItem.get("itemId").toString();
            Item item = Item.valueOf(lineItem.get("item").toString());
            Instant timestamp = Instant.ofEpochMilli((Long) record.get("eventTimestamp"));

            if ("ORDER_CANCELLED".equals(eventType)) {
                // 欠品(eighty-six)由来のイベント。lineItem.name/madeBy は
                // qdca10/qdca10pro 側で欠品検知時点では未確定のため常に null。
                return new TicketUp(orderId, lineItemId, item, null, timestamp, OrderStatus.CANCELLED, null);
            }

            String name = String.valueOf(lineItem.get("name"));
            OrderStatus status = OrderStatus.valueOf(lineItem.get("lineItemStatus").toString());
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
