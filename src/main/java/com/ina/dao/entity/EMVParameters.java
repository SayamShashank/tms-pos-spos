package com.ina.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "emv_parameters")
public class EMVParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;
    @Column(name = "terminal_id", nullable = false)
    private String terminalId;
    @Column(name = "trsmid")
    private String trsmid;
    @Column(name = "device_id",nullable = false)
    private String deviceId;
    @Column(name = "cpks", length = 500000,nullable = false)
    private String cpks;
    @Column(name = "aids", length = 500000,nullable = false)
    private String aids;
    @Column(name = "terminal_config", length = 100000,nullable = false)
    private String terminalConfig;
    @Column(name = "created_date",nullable = false)
    private Timestamp createdDate;
    @Column(name = "updated_date",nullable = false)
    private Timestamp updatedDate;
}
