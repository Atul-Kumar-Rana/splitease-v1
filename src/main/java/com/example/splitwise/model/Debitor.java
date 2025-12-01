package com.example.splitwise.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "debitors")
public class Debitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who this split row belongs to (the person who owes / participates)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-debitors")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonBackReference(value = "event-splits")
    private Event event;

    @Column(precision = 15, scale = 2)
    private BigDecimal debAmount = BigDecimal.ZERO;   // original share

    @Column(precision = 15, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;  // how much paid so far

    private boolean settled = false;

    private boolean included = true; // whether included in split

    private LocalDateTime paidAt;

    @Version
    private Long version; // optimistic locking

    public BigDecimal getRemaining(){
        return debAmount.subtract(amountPaid);
    }

    // getters / setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public User getUser(){ return user; }
    public void setUser(User user){ this.user = user; }
    public Event getEvent(){ return event; }
    public void setEvent(Event event){ this.event = event; }
    public BigDecimal getDebAmount(){ return debAmount; }
    public void setDebAmount(BigDecimal debAmount){ this.debAmount = debAmount; }
    public BigDecimal getAmountPaid(){ return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid){ this.amountPaid = amountPaid; }
    public boolean isSettled(){ return settled; }
    public void setSettled(boolean settled){ this.settled = settled; }
    public boolean isIncluded(){ return included; }
    public void setIncluded(boolean included){ this.included = included; }
    public LocalDateTime getPaidAt(){ return paidAt; }
    public void setPaidAt(LocalDateTime paidAt){ this.paidAt = paidAt; }
}
