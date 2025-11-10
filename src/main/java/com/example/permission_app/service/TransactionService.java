package com.example.permission_app.service;

import com.example.permission_app.model.Transaction;
import com.example.permission_app.model.TransactionStatus;
import com.example.permission_app.repo.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Service
public class TransactionService {
//    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);


    @Autowired
    private  TransactionRepository repo;
    private final SecureRandom secureRandom = new SecureRandom();



    private String newToken() {
        byte[] buf = new byte[24];
        secureRandom.nextBytes(buf);
        return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }


    @Transactional
    public Transaction initiate(Transaction t, String clientRequestId) {
        if (clientRequestId != null && !clientRequestId.isBlank()) {
            Optional<Transaction> existing = repo.findByClientRequestId(clientRequestId);
            if (existing.isPresent()) return existing.get();
            t.setClientRequestId(clientRequestId);
        }
        t.setStatus(TransactionStatus.PENDING_APPROVAL);
        t.setApprovalToken(newToken());
        t.setCreatedAt(Instant.now());
        t.setExpiresAt(Instant.now().plus(DEFAULT_TTL));
        Transaction transactionSaved = repo.save(t);
        // Demo notification: log the approve/reject URLs
//        String approveUrl = "/api/transactions/" + transactionSaved.getId() + "/approve?token=" + transactionSaved.getApprovalToken();
//        String rejectUrl = "/api/transactions/" + transactionSaved.getId() + "/reject?token=" + transactionSaved.getApprovalToken();
//        log.info("Approval requested for txn {}. Approve: {} | Reject: {}", transactionSaved.getId(), approveUrl, rejectUrl);
        return transactionSaved;
    }
    @Transactional
    public Transaction approve(Long id, String token) {
        Transaction tx = repo.findById(id).orElseThrow();
        ensurePendingAndTokenValid(tx, token);
        tx.setStatus(TransactionStatus.APPROVED);
        tx.setDecisionAt(Instant.now());
// Simulate processing and completion
        tx.setStatus(TransactionStatus.COMPLETED);
        return repo.save(tx);
    }


    @Transactional
    public Transaction reject(Long id, String token) {
        Transaction tx = repo.findById(id).orElseThrow();
        ensurePendingAndTokenValid(tx, token);
        tx.setStatus(TransactionStatus.REJECTED);
        tx.setDecisionAt(Instant.now());
        return repo.save(tx);
    }


    private void ensurePendingAndTokenValid(Transaction tx, String token) {
        if (!token.equals(tx.getApprovalToken())) throw new IllegalArgumentException("Invalid token");
        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL)
            throw new IllegalStateException("Transaction not pending approval");
        if (Instant.now().isAfter(tx.getExpiresAt())) {
            tx.setStatus(TransactionStatus.EXPIRED);
            repo.save(tx);
            System.out.println(repo.findById(tx.getId()).get().getStatus());
            throw new IllegalStateException("Approval window expired");
        }
    }

    public List<Transaction> findByOwnerEmailAndStatus(String ownerEmail, TransactionStatus status){
        return repo.findByOwnerEmailAndStatus(ownerEmail, TransactionStatus.PENDING_APPROVAL);
    }
}