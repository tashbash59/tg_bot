package Entity;

import javax.persistence.*;
import java.sql.Time;

@Entity
@Table(name = "orders_time")
public class Orders_time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orders_time_id;

    @Column(name = "order_id", nullable = false)
    private Integer order_id;

    @Column(name = "st", nullable = false)
    private Time st;

    @Column(name = "en", nullable = false)
    private Time en;

    public Integer getOrders_time_id() {
        return orders_time_id;
    }

    public void setOrders_time_id(Integer orders_time_id) {
        this.orders_time_id = orders_time_id;
    }

    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public Time getSt() {
        return st;
    }

    public void setSt(Time st) {
        this.st = st;
    }

    public Time getEn() {
        return en;
    }

    public void setEn(Time en) {
        this.en = en;
    }
}
