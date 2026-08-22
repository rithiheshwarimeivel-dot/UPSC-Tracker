package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Section;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.repository.SectionRepository;
import com.upsc.upsctracker.repository.SubjectRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SectionController {

    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;

    public SectionController(SectionRepository sectionRepository,
                             SubjectRepository subjectRepository) {
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/sections/{subjectId}")
    public String sections(@PathVariable Long subjectId, Model model) {

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow();

        model.addAttribute("subject", subject);

        model.addAttribute("sections",
                sectionRepository.findBySubjectId(subjectId));

        return "sections";
    }

    @GetMapping("/add-section/{subjectId}")
    public String addSectionPage(@PathVariable Long subjectId, Model model) {

        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow();

        Section section = new Section();
        section.setSubject(subject);

        model.addAttribute("section", section);
        model.addAttribute("subject", subject);

        return "add-section";
    }

    @PostMapping("/save-section")
    public String saveSection(@ModelAttribute Section section) {

        sectionRepository.save(section);

        return "redirect:/sections/" + section.getSubject().getId();
    }

    @PostMapping("/delete-section/{id}")
    public String deleteSection(@PathVariable Long id) {

        Section section = sectionRepository
                .findById(id)
                .orElseThrow();

        Long subjectId = section.getSubject().getId();

        sectionRepository.deleteById(id);

        return "redirect:/sections/" + subjectId;
    }
}