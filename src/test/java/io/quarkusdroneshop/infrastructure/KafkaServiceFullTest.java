package io.quarkusdroneshop.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import io.quarkusdroneshop.counter.domain.TestUtil;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@TestProfile(KafkaServiceTestProfile.class)
public class KafkaServiceFullTest {

    @Inject
    KafkaService kafkaService;

    @InjectSpy
    OrderService orderService;

    @Test
    public void testOrderInDelegatesToOrderService() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        OrderEventResult result = TestUtil.stubOrderEventResult();

        doReturn(result).when(orderService).onOrderInTx(any(PlaceOrderCommand.class));
        doNothing().when(orderService).sendOrderUpdate(any());

        kafkaService.orderIn(cmd);

        verify(orderService, times(1)).onOrderInTx(any(PlaceOrderCommand.class));
    }

    @Test
    public void testOrderUpDelegatesToOrderService() {
        TicketUp ticketUp = TestUtil.stubOrderTicketUp();
        OrderEventResult result = new OrderEventResult(Collections.emptyList());

        doReturn(result).when(orderService).onOrderUpTx(any(TicketUp.class));

        kafkaService.orderUp(ticketUp);

        verify(orderService, times(1)).onOrderUpTx(any(TicketUp.class));
    }

    @Test
    public void testOrderUpWithNullTicketUpIsIgnored() {
        kafkaService.orderUp(null);
        verify(orderService, never()).onOrderUpTx(any());
    }

    @Test
    public void testOrderUpWithNullOrderIdIsIgnored() {
        TicketUp ticketUp = new TicketUp(null, null, Item.QDC_A101, "Taro", "Worker");

        kafkaService.orderUp(ticketUp);

        verify(orderService, never()).onOrderUpTx(any());
    }

}
