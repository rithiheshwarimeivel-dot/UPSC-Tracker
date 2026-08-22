package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.ExamType;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.repository.SubjectRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SubjectController {

    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
   }

    @GetMapping("/subjects")
    public String subjects(Model model) {

        model.addAttribute("prelimsSubjects",
                subjectRepository.findByExamType(ExamType.PRELIMS));

        model.addAttribute("mainsSubjects",
                subjectRepository.findByExamType(ExamType.MAINS));

        return "subjects";
    }

    @GetMapping("/add-subject")
    public String addSubjectPage(Model model) {

        model.addAttribute("subject", new Subject());

        return "add-subject";
    }

    @PostMapping("/save-subject")
    public String saveSubject(@ModelAttribute Subject subject) {

        subjectRepository.save(subject);

        return "redirect:/subjects";
    }

    @GetMapping("/edit-subject/{id}")
    public String editSubject(@PathVariable Long id, Model model) {

        Subject subject = subjectRepository.findById(id).orElseThrow();

        model.addAttribute("subject", subject);

        return "edit-subject";
    }

    @PostMapping("/update-subject")
    public String updateSubject(@ModelAttribute Subject subject) {

        subjectRepository.save(subject);

        return "redirect:/subjects";
    }

    @PostMapping("/delete-subject/{id}")
    public String deleteSubject(@PathVariable Long id) {

        subjectRepository.deleteById(id);

        return "redirect:/subjects";
    }
}