package com.example.permission_app.controller;

import com.example.permission_app.model.Transaction;
import com.example.permission_app.model.TransactionStatus;
import com.example.permission_app.repo.TransactionRepository;
import com.example.permission_app.service.TransactionService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/transactions")
//@CrossOrigin
public class TransactionController {


    @Autowired
    private  TransactionService service;
    @Autowired
    private  TransactionRepository repo;


//    public TransactionController(TransactionService service, TransactionRepository repo) {
//        this.service = service;
//        this.repo = repo;
//    }


    // DTOs
    public record CreateTxnRequest(
            @NotBlank String accountId,
            @NotBlank @Email String ownerEmail,
            @NotNull BigDecimal amount,
            @NotBlank String merchant,
            String clientRequestId
    ) {}

    public record TxnResponse(Long id, String status, String approvalToken, Instant expiresAt) {}


    @PostMapping
    public ResponseEntity<TxnResponse> create(@RequestBody CreateTxnRequest req) {
        Transaction t = new Transaction();
        t.setAccountId(req.accountId());
        t.setOwnerEmail(req.ownerEmail());
        t.setAmount(req.amount());
        t.setMerchant(req.merchant());
        t.setStatus(TransactionStatus.INITIATED);
        Transaction saved = service.initiate(t, req.clientRequestId());
        return ResponseEntity.ok(new TxnResponse(saved.getId(), saved.getStatus().name(), saved.getApprovalToken(), saved.getExpiresAt()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Transaction> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/pending")
    public List<Transaction> pending(@RequestParam @Email String ownerEmail) {
        return service.findByOwnerEmailAndStatus(ownerEmail, TransactionStatus.PENDING_APPROVAL);
    }


    @PostMapping("/{id}/approve")
    public ResponseEntity<Transaction> approve(@PathVariable Long id, @RequestParam String token) {
        return ResponseEntity.ok(service.approve(id, token));
    }


    @PostMapping("/{id}/reject")
    public ResponseEntity<Transaction> reject(@PathVariable Long id, @RequestParam String token) {
        return ResponseEntity.ok(service.reject(id, token));
    }
}
