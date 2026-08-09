package com.feel.backend.repository;

import com.feel.backend.entity.PledgeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PledgeProgressRepository extends JpaRepository<PledgeProgress, String> {

    List<PledgeProgress> findAllByOrderById();
}
