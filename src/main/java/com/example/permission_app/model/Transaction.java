package com.example.permission_app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;


@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "ix_client_req", columnList = "clientRequestId", unique = true),
        @Index(name = "ix_token", columnList = "approvalToken", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String accountId;
    private String ownerEmail; // demo purpose; production: userId + verified contact channel


    private BigDecimal amount;
    private String merchant;


    @Enumerated(EnumType.STRING)
    private TransactionStatus status;


    private String approvalToken;
    private Instant expiresAt;


    private Instant createdAt;
    private Instant decisionAt;


    private String clientRequestId; // idempotency key from client (optional)
}