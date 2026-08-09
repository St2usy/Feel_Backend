package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pledge_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PledgeProgress {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;
}
