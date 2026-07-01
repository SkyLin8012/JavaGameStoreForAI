package com.steam.score.repository;

import com.steam.score.model.ScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA 資料庫操作接口
 */
@Repository
public interface ScoreRepository extends JpaRepository<ScoreRecord, Long> {
}
