package com.feel.backend.service;

import com.feel.backend.dto.PledgeProgressDto;
import com.feel.backend.entity.PledgeProgress;
import com.feel.backend.repository.PledgeProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PledgeProgressService {

    private final PledgeProgressRepository pledgeProgressRepository;

    /**
     * 전체 공약 ID별 이행 여부 조회 (공개 API, JBNU 사이트에서 사용)
     */
    @Transactional(readOnly = true)
    public PledgeProgressDto.ProgressResponse getProgress() {
        List<PledgeProgress> list = pledgeProgressRepository.findAllByOrderById();
        Map<String, Boolean> progress = list.stream()
            .collect(Collectors.toMap(PledgeProgress::getId, PledgeProgress::getCompleted, (a, b) -> a, LinkedHashMap::new));
        return PledgeProgressDto.ProgressResponse.builder().progress(progress).build();
    }

    /**
     * 단건 이행 여부 수정 (관리자)
     */
    @Transactional
    public PledgeProgressDto.ProgressResponse updateProgress(String id, Boolean completed) {
        PledgeProgress entity = pledgeProgressRepository.findById(id)
            .orElse(PledgeProgress.builder().id(id).completed(false).build());
        entity.setCompleted(Boolean.TRUE.equals(completed));
        pledgeProgressRepository.save(entity);
        return getProgress();
    }

    /**
     * 일괄 이행 여부 수정 (관리자)
     */
    @Transactional
    public PledgeProgressDto.ProgressResponse updateProgressBatch(Map<String, Boolean> progress) {
        if (progress == null || progress.isEmpty()) {
            return getProgress();
        }
        for (Map.Entry<String, Boolean> e : progress.entrySet()) {
            String id = e.getKey();
            if (id == null || id.isBlank()) continue;
            PledgeProgress entity = pledgeProgressRepository.findById(id)
                .orElse(PledgeProgress.builder().id(id).completed(false).build());
            entity.setCompleted(Boolean.TRUE.equals(e.getValue()));
            pledgeProgressRepository.save(entity);
        }
        return getProgress();
    }
}
