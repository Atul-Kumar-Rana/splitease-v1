package com.example.splitwise.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.splitwise.model.Debitor;

import java.util.List;

public interface DebitorRepo extends JpaRepository<Debitor, Long> {
    List<Debitor> findByUserId(Long userId);
}
