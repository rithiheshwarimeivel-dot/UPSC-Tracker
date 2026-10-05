package com.upsc.upsctracker.service;

import com.upsc.upsctracker.entity.Revision;
import com.upsc.upsctracker.entity.RevisionHistory;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.RevisionHistoryRepository;
import com.upsc.upsctracker.repository.RevisionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RevisionService {

    private final RevisionRepository revisionRepository;
    private final RevisionHistoryRepository revisionHistoryRepository;

    public RevisionService(
            RevisionRepository revisionRepository,
            RevisionHistoryRepository revisionHistoryRepository) {

        this.revisionRepository = revisionRepository;
        this.revisionHistoryRepository = revisionHistoryRepository;
    }


    // ==================================================
    // AUTOMATICALLY CREATE REVISION FOR COMPLETED TOPIC
    //
    // Revision 1 -> 2 days after completion
    // ==================================================

    @Transactional
    public Revision createRevisionForTopic(Topic topic) {

        if (topic == null || topic.getId() == null) {
            throw new IllegalArgumentException(
                    "Topic cannot be null"
            );
        }


        // ==================================================
        // DO NOT CREATE DUPLICATE REVISION
        // ==================================================

        if (revisionRepository.existsByTopicId(topic.getId())) {

            return revisionRepository
                    .findByTopicId(topic.getId())
                    .orElseThrow();
        }


        // ==================================================
        // CREATE NEW REVISION
        // ==================================================

        Revision revision = new Revision();

        revision.setTopic(topic);

        revision.setRevisionCount(0);

        revision.setCurrentStatus("NOT_REVISED");

        // Revision 1 is due 2 days after completion
        revision.setNextRevisionDate(
                LocalDate.now().plusDays(2)
        );


        return revisionRepository.save(revision);
    }


    // ==================================================
    // REMOVE REVISION WHEN TOPIC BECOMES INCOMPLETE
    // ==================================================

    @Transactional
    public void removeRevisionForTopic(Long topicId) {

        if (topicId == null) {
            return;
        }


        revisionRepository
                .findByTopicId(topicId)
                .ifPresent(revision -> {

                    // Delete history first because of FK
                    List<RevisionHistory> history =
                            revisionHistoryRepository
                                    .findByRevisionIdOrderByRevisionNumberAsc(
                                            revision.getId()
                                    );

                    if (!history.isEmpty()) {

                        revisionHistoryRepository
                                .deleteAll(history);
                    }


                    revisionRepository.delete(revision);
                });
    }


    // ==================================================
    // GET ALL REVISIONS
    // ==================================================

    public List<Revision> getAllRevisions() {

        return revisionRepository
                .findAllByOrderByNextRevisionDateAsc();
    }


    // ==================================================
    // GET ONE REVISION
    // ==================================================

    public Revision getRevisionById(Integer id) {

        return revisionRepository
                .findById(id)
                .orElseThrow();
    }


    // ==================================================
    // MARK REVISION AS REVISED
    //
    // Revision 1 -> Revision 2
    // Revision 2 -> Revision 3
    // Revision 3 -> Revision 4
    // Revision 4 -> Revision 5
    // Revision 5 -> Revision 6
    // Revision 6 -> Completed
    // ==================================================

    @Transactional
    public Revision markAsRevised(
            Integer revisionId,
            String notes) {

        Revision revision =
                revisionRepository
                        .findById(revisionId)
                        .orElseThrow();


        int currentCount =
                revision.getRevisionCount() == null
                        ? 0
                        : revision.getRevisionCount();


        // ==================================================
        // MAXIMUM STANDARD REVISIONS = 6
        // ==================================================

        if (currentCount >= 6) {

            revision.setCurrentStatus("COMPLETED");

            revision.setNextRevisionDate(null);

            return revisionRepository.save(revision);
        }


        // ==================================================
        // THIS REVISION NUMBER
        // ==================================================

        int revisionNumber =
                currentCount + 1;


        // ==================================================
        // SAVE HISTORY
        // ==================================================

        RevisionHistory history =
                new RevisionHistory();

        history.setRevision(revision);

        history.setRevisionNumber(
                revisionNumber
        );

        history.setNotes(notes);

        revisionHistoryRepository.save(history);


        // ==================================================
        // UPDATE REVISION COUNT
        // ==================================================

        revision.setRevisionCount(
                revisionNumber
        );


        // ==================================================
        // SET NEXT REVISION
        // ==================================================

        LocalDate today =
                LocalDate.now();


        switch (revisionNumber) {

            case 1:
                // Revision 2 -> 7 days later
                revision.setNextRevisionDate(
                        today.plusDays(7)
                );
                revision.setCurrentStatus(
                        "UPCOMING"
                );
                break;


            case 2:
                // Revision 3 -> 15 days later
                revision.setNextRevisionDate(
                        today.plusDays(15)
                );
                revision.setCurrentStatus(
                        "UPCOMING"
                );
                break;


            case 3:
                // Revision 4 -> 30 days later
                revision.setNextRevisionDate(
                        today.plusDays(30)
                );
                revision.setCurrentStatus(
                        "UPCOMING"
                );
                break;


            case 4:
                // Revision 5 -> 60 days later
                revision.setNextRevisionDate(
                        today.plusDays(60)
                );
                revision.setCurrentStatus(
                        "UPCOMING"
                );
                break;


            case 5:
                // Revision 6 -> 90 days later
                revision.setNextRevisionDate(
                        today.plusDays(90)
                );
                revision.setCurrentStatus(
                        "UPCOMING"
                );
                break;


            case 6:
                // All standard revisions completed
                revision.setNextRevisionDate(null);

                revision.setCurrentStatus(
                        "COMPLETED"
                );
                break;


            default:
                revision.setNextRevisionDate(null);

                revision.setCurrentStatus(
                        "COMPLETED"
                );
        }


        return revisionRepository.save(revision);
    }


    // ==================================================
    // GET REVISION HISTORY
    // ==================================================

    public List<RevisionHistory> getRevisionHistory(
            Integer revisionId) {

        return revisionHistoryRepository
                .findByRevisionIdOrderByRevisionNumberAsc(
                        revisionId
                );
    }


    // ==================================================
    // ADD EXTRA REVISION
    // USED AFTER 6 STANDARD REVISIONS
    // ==================================================

    @Transactional
    public Revision addExtraRevision(
            Integer revisionId,
            LocalDate nextDate) {

        Revision revision =
                revisionRepository
                        .findById(revisionId)
                        .orElseThrow();


        if (nextDate == null) {

            throw new IllegalArgumentException(
                    "Revision date is required"
            );
        }


        revision.setNextRevisionDate(
                nextDate
        );

        revision.setCurrentStatus(
                "UPCOMING"
        );


        return revisionRepository.save(revision);
    }
}