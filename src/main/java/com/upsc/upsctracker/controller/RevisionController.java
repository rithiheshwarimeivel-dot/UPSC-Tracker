package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Revision;
import com.upsc.upsctracker.entity.RevisionHistory;
import com.upsc.upsctracker.service.RevisionService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class RevisionController {

    private final RevisionService revisionService;

    public RevisionController(
            RevisionService revisionService) {

        this.revisionService = revisionService;
    }


    // ==================================================
    // REVISION PAGE
    // URL: /revision
    // ==================================================

    @GetMapping("/revision")
    public String revisionPage(
            Model model) {

        List<Revision> revisions =
                revisionService.getAllRevisions();

        LocalDate today =
                LocalDate.now();


        // ==================================================
        // ADD STATUS INFORMATION
        // ==================================================

        for (Revision revision : revisions) {

            LocalDate date =
                    revision.getNextRevisionDate();

            String status;


            if (date == null) {

                status = "COMPLETED";

            } else if (date.isBefore(today)) {

                status = "OVERDUE";

            } else if (date.isEqual(today)) {

                status = "DUE_TODAY";

            } else {

                status = "UPCOMING";
            }


            revision.setCurrentStatus(status);
        }


        model.addAttribute(
                "revisions",
                revisions
        );

        model.addAttribute(
                "today",
                today
        );


        return "revision";
    }


    // ==================================================
    // OPEN SINGLE REVISION
    // URL: /revision/{id}
    // ==================================================

    @GetMapping("/revision/{id}")
    public String viewRevision(
            @PathVariable Integer id,
            Model model) {

        Revision revision =
                revisionService
                        .getRevisionById(id);


        List<RevisionHistory> history =
                revisionService
                        .getRevisionHistory(id);


        model.addAttribute(
                "revision",
                revision
        );

        model.addAttribute(
                "history",
                history
        );


        return "revision-detail";
    }


    // ==================================================
    // MARK REVISION AS REVISED
    // URL: /revision/{id}/revised
    // ==================================================

    @PostMapping("/revision/{id}/revised")
    public String markRevised(
            @PathVariable Integer id,
            @RequestParam(
                    value = "notes",
                    required = false
            )
            String notes) {

        revisionService.markAsRevised(
                id,
                notes
        );


        // Stay on the SAME revision detail page
        return "redirect:/revision/" + id;
    }


    // ==================================================
    // ADD EXTRA REVISION
    // ==================================================

    @PostMapping("/revision/{id}/add-extra")
    public String addExtraRevision(
            @PathVariable Integer id,
            @RequestParam("nextDate")
            String nextDate) {

        LocalDate date =
                LocalDate.parse(nextDate);


        revisionService.addExtraRevision(
                id,
                date
        );


        return "redirect:/revision/" + id;
    }
}