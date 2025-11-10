# Payment-Permission-app

# A approval-based payment flow:

1. User triggers a payment request.
2. Payment goes to **PENDING_APPROVAL**.
3. Approver views pending payments.
4. Approver can **Approve** (payment proceeds) or **Reject** (payment should not go through).

## Flow (High Level)

1. **User** initiates a transaction → status = `INITIATED` or `PENDING_APPROVAL`
2. **System** holds the transaction until someone approves it
3. **Approver** sees all `PENDING_APPROVAL` transactions
4. Approver:
   - **Approve** → try to process payment
   - **Reject** → mark as `REJECTED` (no money should move)
5. Payment processing:
   - Success → `COMPLETED`
   - Fail → `FAILED`
6. (Optional) If not approved within time → `EXPIRED`
