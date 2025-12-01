package com.waseeq.BFH.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "solution")
public class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String regNo;
    private String questionId;

    @Column(length = 8000)
    private String finalQuery;

    private String webhookUrl;
    private String accessTokenUsed;
    private String submissionStatus;

    @Column(length = 8000)
    private String submissionResponse;

    @CreationTimestamp
    private Instant createdAt;
}