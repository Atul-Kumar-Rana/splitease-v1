package com.example.splitwise.service;

import com.example.splitwise.model.Debitor;
import com.example.splitwise.model.Event;
import com.example.splitwise.model.User;
import com.example.splitwise.repo.DebitorRepo;
import com.example.splitwise.repo.EventRepo;
import com.example.splitwise.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DebitorService {

    private final DebitorRepo debitorRepo;
    private final EventRepo eventRepo;
    private final UserRepo userRepo;

    public DebitorService(DebitorRepo debitorRepo, EventRepo eventRepo, UserRepo userRepo){
        this.debitorRepo = debitorRepo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
    }

    /**
     * Add a Debitor to an event. Debitor.d.user.id must be set by caller.
     */
    @Transactional
    public Debitor addDebitorToEvent(Long eventId, Debitor d){
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        if (d.getUser() == null || d.getUser().getId() == null) {
            throw new IllegalArgumentException("Debitor.user.id is required");
        }

        User u = userRepo.findById(d.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + d.getUser().getId()));

        d.setEvent(event);
        d.setUser(u);

        Debitor saved = debitorRepo.save(d);

        // keep bidirectional relation consistent (in-memory)
        event.getSplits().add(saved);

        return saved;
    }

    /**
     * Delete a debitor by id.
     */
    @Transactional
    public void deleteDebitor(Long debitorId){
        if (!debitorRepo.existsById(debitorId))
            throw new IllegalArgumentException("Debitor not found: " + debitorId);
        debitorRepo.deleteById(debitorId);
    }

    /**
     * Get a debitor by id.
     */
    @Transactional(readOnly = true)
    public Debitor getDebitor(Long id){
        return debitorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debitor not found: " + id));
    }

    /**
     * List all debitors (global).
     */
    @Transactional(readOnly = true)
    public List<Debitor> getAllDebitors(){
        return debitorRepo.findAll();
    }

    /**
     * List debitors for a specific event.
     * Uses in-transaction filtering (safe for small datasets).
     */
    @Transactional(readOnly = true)
    public List<Debitor> getDebitorsByEvent(Long eventId){
        // if your DebitorRepo has a method like findByEventId, replace this with that call.
        return debitorRepo.findAll().stream()
                .filter(d -> d.getEvent() != null && eventId.equals(d.getEvent().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Update a debitor (only allowed fields).
     */
    @Transactional
    public Debitor updateDebitor(Long id, Debitor payload){
        Debitor existing = debitorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debitor not found: " + id));

        // update only safe/simple fields
        if (payload.getDebAmount() != null) existing.setDebAmount(payload.getDebAmount());
        if (payload.getAmountPaid() != null) existing.setAmountPaid(payload.getAmountPaid());
        existing.setIncluded(payload.isIncluded());
        existing.setSettled(payload.isSettled());
        if (payload.getPaidAt() != null) existing.setPaidAt(payload.getPaidAt());

        return debitorRepo.save(existing);
    }
}
