package com.example.splitwise.controllers;

import com.example.splitwise.model.Debitor;
import com.example.splitwise.service.DebitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DebitorController {

    private final DebitorService debitorService;

    public DebitorController(DebitorService debitorService){
        this.debitorService = debitorService;
    }

    // Canonical path: POST /api/events/{eventId}/debitors
    @PostMapping("/events/{eventId}/debitors")
    public ResponseEntity<?> addDebitorToEventByEventPath(
            @PathVariable Long eventId,
            @RequestBody Debitor d) {
        try {
            Debitor saved = debitorService.addDebitorToEvent(eventId, d);
            Map<String,Object> resp = Map.of(
                    "id", saved.getId(),
                    "userId", saved.getUser().getId(),
                    "debAmount", saved.getDebAmount(),
                    "included", saved.isIncluded()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","add_debitor_failed","message", ex.getMessage()));
        }
    }

    // Alternate (keeps backward compat): POST /api/debitors/{eventId}
    @PostMapping("/debitors/{eventId}")
    public ResponseEntity<?> addDebitorToEventByDebitorPath(
            @PathVariable Long eventId,
            @RequestBody Debitor d) {
        return addDebitorToEventByEventPath(eventId, d);
    }

    // DELETE /api/debitors/{debitorId}
    @DeleteMapping("/debitors/{debitorId}")
    public ResponseEntity<?> deleteDebitor(@PathVariable Long debitorId){
        try {
            debitorService.deleteDebitor(debitorId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","delete_failed", "message", ex.getMessage()));
        }
    }

}
