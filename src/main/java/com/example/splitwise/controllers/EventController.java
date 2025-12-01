package com.example.splitwise.controllers;
import com.example.splitwise.model.Debitor;
import com.example.splitwise.model.Event;
import com.example.splitwise.model.User;
import com.example.splitwise.service.EventService;
import com.example.splitwise.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService){
        this.eventService = eventService;
        this.userService = userService;
    }

    // DTOs (simple, nested)
    public static class ParticipantDto {
        public Long userId;
        public boolean included = true;
        // optional custom share omitted for simplicity
    }
    public static class CreateEventDto {
        public String title;
        public Long creatorId;
        public BigDecimal total;
        public List<ParticipantDto> participants = new ArrayList<>();
    }

    // Create event with participants (equal split among included)
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody CreateEventDto dto){
        // validate creator
        User creator = userService.getUser(dto.creatorId).orElse(null);
        if (creator == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        if (dto.total == null || dto.total.compareTo(BigDecimal.ZERO) <= 0) return ResponseEntity.badRequest().build();

        Event e = new Event();
        e.setTitle(dto.title);
        e.setCreator(creator);
        e.setTotal(dto.total);

        // build participant users list (only included)
        List<User> includedUsers = dto.participants.stream()
                .filter(p -> p.included)
                .map(p -> userService.getUser(p.userId).orElse(null))
                .collect(Collectors.toList());

        if (includedUsers.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        // create equal splits and attach to event
        eventService.createEqualSplits(e, includedUsers);

        // persist event + splits
        Event saved = eventService.createEvent(e, e.getSplits());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Get event by id
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id){
        try {
            Event e = eventService.getEvent(id);
            return ResponseEntity.ok(e);
        } catch (IllegalArgumentException ex){
            return ResponseEntity.notFound().build();
        }
    }

    // List events
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(){
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // Delete event (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex){
            return ResponseEntity.notFound().build();
        }
    }

    // Cancel (soft delete)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Event> cancelEvent(@PathVariable Long id){
        try {
            Event e = eventService.cancelEvent(id);
            return ResponseEntity.ok(e);
        } catch (IllegalArgumentException ex){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{eventId}/debitors")
    public ResponseEntity<?> getDebitorsByEvent(@PathVariable Long eventId) {
        Event e;
        try {
            e = eventService.getEvent(eventId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "event not found"));
        }

        var list = e.getSplits().stream()
                .map(d -> Map.of(
                        "debitorId", d.getId(),
                        "userId", d.getUser().getId(),
                        "username", d.getUser().getUsername(),
                        "debAmount", d.getDebAmount(),
                        "paid", d.getAmountPaid(),
                        "remaining", d.getRemaining(),
                        "included", d.isIncluded()
                ))
                .toList();

        return ResponseEntity.ok(list);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody CreateEventDto payload) {

        // load event safely
        Event existing;
        try {
            existing = eventService.getEvent(id);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "event not found"));
        }

        // update only allowed fields
        if (payload.title != null) existing.setTitle(payload.title);
        if (payload.total != null) existing.setTotal(payload.total);

        // SAVE
        Event saved = eventService.save(existing);  // ← you MUST add save() in EventService if missing
        return ResponseEntity.ok(saved);
    }

}
