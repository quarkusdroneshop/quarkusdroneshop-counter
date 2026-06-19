package io.quarkusdroneshop.counter.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testQdcA101Price() {
        assertEquals(BigDecimal.valueOf(135.50), Item.QDC_A101.getPrice());
    }

    @Test
    public void testQdcA102Price() {
        assertEquals(BigDecimal.valueOf(155.50), Item.QDC_A102.getPrice());
    }

    @Test
    public void testQdcA103Price() {
        assertEquals(BigDecimal.valueOf(144.00), Item.QDC_A103.getPrice());
    }

    @Test
    public void testQdcA104AcPrice() {
        assertEquals(BigDecimal.valueOf(256.25), Item.QDC_A104_AC.getPrice());
    }

    @Test
    public void testQdcA104AtPrice() {
        assertEquals(BigDecimal.valueOf(305.75), Item.QDC_A104_AT.getPrice());
    }

    @Test
    public void testQdcA105Pro01Price() {
        assertEquals(BigDecimal.valueOf(553.00), Item.QDC_A105_Pro01.getPrice());
    }

    @Test
    public void testQdcA105Pro02Price() {
        assertEquals(BigDecimal.valueOf(633.25), Item.QDC_A105_Pro02.getPrice());
    }

    @Test
    public void testQdcA105Pro03Price() {
        assertEquals(BigDecimal.valueOf(735.50), Item.QDC_A105_Pro03.getPrice());
    }

    @Test
    public void testQdcA105Pro04Price() {
        assertEquals(BigDecimal.valueOf(955.50), Item.QDC_A105_Pro04.getPrice());
    }

    @Test
    public void testEnumValues() {
        assertEquals(9, Item.values().length);
        assertEquals(Item.QDC_A101, Item.valueOf("QDC_A101"));
    }
}
