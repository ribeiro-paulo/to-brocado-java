package br.edu.icomp.ufam.to_brocado.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Bill {

    public static final BigDecimal SERVICE_RATE = new BigDecimal("0.10");

    private final BigDecimal subtotal;
    private final BigDecimal serviceFee;
    private final BigDecimal total;
    private final BigDecimal perPerson;

    public Bill(BigDecimal subtotal, boolean includeServiceFee, int people) {
        if (people < 1) {
            throw new IllegalArgumentException("people must be at least 1");
        }
        this.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        this.serviceFee = includeServiceFee
                ? this.subtotal.multiply(SERVICE_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        this.total = this.subtotal.add(serviceFee);
        this.perPerson = total.divide(BigDecimal.valueOf(people), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPerPerson() {
        return perPerson;
    }
}
