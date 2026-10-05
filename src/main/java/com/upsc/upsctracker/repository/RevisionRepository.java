package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.Revision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RevisionRepository
        extends JpaRepository<Revision, Integer> {

    /*
     * Check whether a revision already exists
     * for a particular topic.
     */
    boolean existsByTopicId(Long topicId);


    /*
     * Find the revision belonging to a particular topic.
     */
    Optional<Revision> findByTopicId(Long topicId);


    /*
     * Get all revisions ordered by the next
     * scheduled revision date.
     */
    List<Revision> findAllByOrderByNextRevisionDateAsc();
}