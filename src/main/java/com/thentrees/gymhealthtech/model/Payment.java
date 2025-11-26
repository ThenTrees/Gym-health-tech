package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends BaseEntity {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String provider; // 'SEPAY'

   @Column(name = "provider_txn_id")
  private String providerTxnId; // 'SEPAY'

  @Column(name = "amount_cents", nullable = false)
  private Long amount; // in VND (e.g., 99000)

  @Column(nullable = false)
  private PaymentStatus status; // PENDING, SUCCESS, FAILED

  @Column(name = "transaction_code")
  private String transactionCode; // e.g. description 'SUB-USER-<id>-<nonce>'

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "meta", columnDefinition = "jsonb")
  private String rawResponse;
}
