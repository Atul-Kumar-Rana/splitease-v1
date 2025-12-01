package com.example.splitwise.service;

import com.example.splitwise.model.Debitor;
import com.example.splitwise.model.Transaction;
import com.example.splitwise.model.User;
import com.example.splitwise.repo.DebitorRepo;
import com.example.splitwise.repo.TransactionRepo;
import com.example.splitwise.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final DebitorRepo debitorRepo;
    private final TransactionRepo transactionRepo;
    private final UserRepo userRepo;

    public PaymentService(DebitorRepo debitorRepo, TransactionRepo transactionRepo, UserRepo userRepo){
        this.debitorRepo = debitorRepo;
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
    }

    /**
     * Payer pays part or full of a Debitor (split).
     * Creates a Transaction and updates the Debitor.
     */
    @Transactional
    public Transaction payDebitor(Long debitorId, Long payerUserId, BigDecimal amount){
        Debitor split = debitorRepo.findById(debitorId)
                .orElseThrow(() -> new IllegalArgumentException("Split not found"));
        User payer = userRepo.findById(payerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Payer not found"));

        User receiver = split.getEvent() != null ? split.getEvent().getCreator() : null;
        if (receiver == null) throw new IllegalStateException("Receiver not found for this split");

        BigDecimal remaining = split.getRemaining();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Invalid amount");
        if (amount.compareTo(remaining) > 0) throw new IllegalArgumentException("Amount exceeds remaining share");

        Transaction tx = new Transaction();
        tx.setFromUser(payer);
        tx.setToUser(receiver);
        tx.setAmount(amount);
        tx.setEventId(split.getEvent() != null ? split.getEvent().getId() : null);
        tx.setTs(LocalDateTime.now());
        transactionRepo.save(tx);

        split.setAmountPaid(split.getAmountPaid().add(amount));
        if (split.getAmountPaid().compareTo(split.getDebAmount()) >= 0){
            split.setSettled(true);
            split.setPaidAt(LocalDateTime.now());
        }
        debitorRepo.save(split);

        // optional: update materialized user totals if used (do inside same tx)
        // payer.setTotal(payer.getTotal().subtract(amount)); userRepo.save(payer);
        // receiver.setTotal(receiver.getTotal().add(amount)); userRepo.save(receiver);

        return tx;
    }
}
