package com.example.permission_app.repo;


import com.example.permission_app.model.Transaction;
import com.example.permission_app.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByApprovalToken(String approvalToken);
    Optional<Transaction> findByClientRequestId(String clientRequestId);
    List<Transaction> findByOwnerEmailAndStatus(String ownerEmail, TransactionStatus status);
}