package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.RevisionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevisionHistoryRepository
        extends JpaRepository<RevisionHistory, Integer> {

    /*
     * Get revision history for one revision,
     * ordered from Revision 1 → Revision 6.
     */
    List<RevisionHistory>
    findByRevisionIdOrderByRevisionNumberAsc(
            Integer revisionId
    );
}