package com.example.splitwise.repo;

import com.example.splitwise.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepo extends JpaRepository<Event, Long> {
    @Query("select e from Event e left join fetch e.splits where e.id = :id")
    Event findWithSplitsById(@Param("id") Long id);
}
