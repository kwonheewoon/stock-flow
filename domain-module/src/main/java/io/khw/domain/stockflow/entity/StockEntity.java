package io.khw.domain.stockflow.entity;import io.khw.domain.common.entity.AuditEntity;import io.khw.domain.popularsearchkeyword.entity.BaseEntity;import jakarta.persistence.*;import lombok.Getter;import lombok.NoArgsConstructor;import org.springframework.data.relational.core.mapping.Column;import org.springframework.data.relational.core.mapping.Table;import java.io.Serializable;import java.math.BigDecimal;@NoArgsConstructor@Getter@Table(name = "stock")@Entitypublic class StockEntity implements Serializable {    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    @Column("id")    private Long id;    @Column("code")    private String code;    @Column("name")    private String name;    @Column("price")    private BigDecimal price;    @Embedded    private AuditEntity auditEntity;}