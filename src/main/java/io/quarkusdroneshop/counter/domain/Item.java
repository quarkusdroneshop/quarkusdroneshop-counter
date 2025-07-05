package io.quarkusdroneshop.counter.domain;

import java.math.BigDecimal;

/**
 * Models the Menu Item
 */
public enum Item {

    //QD-A10 SERIES
    QDC_A101(BigDecimal.valueOf(135.50)), QDC_A102(BigDecimal.valueOf(155.50)), QDC_A103(BigDecimal.valueOf(144.00)), QDC_A104_AC(BigDecimal.valueOf(256.25)), QDC_A104_AT(BigDecimal.valueOf(305.75)),

    //QD-A10Pro SERIES
    QDC_A105_Pro01(BigDecimal.valueOf(553.00)), QDC_A105_Pro02(BigDecimal.valueOf(633.25)), QDC_A105_Pro03(BigDecimal.valueOf(735.50)), QDC_A105_Pro04(BigDecimal.valueOf(955.50));

    private BigDecimal price;

  public BigDecimal getPrice() {
    return this.price;
  }

  private Item(BigDecimal price) {
    this.price = price;
  }

}