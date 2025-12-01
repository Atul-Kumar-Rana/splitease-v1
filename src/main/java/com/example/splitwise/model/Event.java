package com.example.splitwise.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime createdAt = LocalDateTime.now();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @JsonBackReference(value = "user-events")
    private User creator;

    @Column(precision = 15, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    private boolean cancelled = false;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "event-splits")
    private List<Debitor> splits = new ArrayList<>();

    // helpers
    public void addSplit(Debitor d){
        splits.add(d);
        d.setEvent(this);
    }
    public void removeSplit(Debitor d){
        splits.remove(d);
        d.setEvent(null);
    }

    // getters / setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }
    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title = title; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public User getCreator(){ return creator; }
    public void setCreator(User creator){ this.creator = creator; }
    public BigDecimal getTotal(){ return total; }
    public void setTotal(BigDecimal total){ this.total = total; }
    public List<Debitor> getSplits(){ return splits; }
    public boolean isCancelled(){ return cancelled; }
    public void setCancelled(boolean cancelled){ this.cancelled = cancelled; }
}
