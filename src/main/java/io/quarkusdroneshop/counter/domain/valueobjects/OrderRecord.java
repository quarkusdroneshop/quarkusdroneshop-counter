package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkusdroneshop.counter.domain.LineItem;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class OrderRecord extends PanacheEntityBase {

    @Id
    private String orderId;

    @OneToMany(mappedBy = "order")
    private List<LineItem> lineItemList;

    // コンストラクタやgetter/setterを定義

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<LineItem> getLineItemList() {
        return lineItemList;
    }

    public void setLineItemList(List<LineItem> lineItemList) {
        this.lineItemList = lineItemList;
    }

    // 必要に応じてコンストラクタなども実装
}