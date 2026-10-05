package com.upsc.upsctracker.controller;

import com.upsc.upsctracker.entity.Section;
import com.upsc.upsctracker.entity.Subject;
import com.upsc.upsctracker.entity.Topic;
import com.upsc.upsctracker.repository.SectionRepository;
import com.upsc.upsctracker.repository.SubjectRepository;
import com.upsc.upsctracker.repository.TopicRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CurrentAffairsController {

    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public CurrentAffairsController(
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    // =========================================================
    // CURRENT AFFAIRS HOME
    // =========================================================

    @GetMapping("/current-affairs")
    public String currentAffairs(Model model, HttpSession session) {

        Integer attemptYear = getAttemptYearFromSession(session);

        if (attemptYear != null) {
            model.addAttribute("attemptYear", attemptYear);
        }

        return "current-affairs";
    }

    // =========================================================
    // PRELIMS CURRENT AFFAIRS
    // =========================================================

    @GetMapping("/current-affairs/prelims")
    public String showPrelims(Model model, HttpSession session) {

        Integer attemptYear = getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Subject subject = getCurrentAffairsSubject();

        int previousYear = attemptYear - 1;

        String[] months = getMonths();

        List<Section> previousYearSections = new ArrayList<>();
        List<Section> attemptYearSections = new ArrayList<>();

        // Previous year - all 12 months
        for (String month : months) {

            Section section =
                    getOrCreatePrelimsSection(
                            month,
                            previousYear,
                            subject
                    );

            createPrelimsTopics(section);

            previousYearSections.add(section);
        }

        // Attempt year - January to May
        for (int i = 0; i < 5; i++) {

            Section section =
                    getOrCreatePrelimsSection(
                            months[i],
                            attemptYear,
                            subject
                    );

            createPrelimsTopics(section);

            attemptYearSections.add(section);
        }

        int previousYearTotalTopics = 0;
        int previousYearCompletedTopics = 0;

        for (Section section : previousYearSections) {

            List<Topic> topics =
                    getTopicsForSection(section.getId());

            previousYearTotalTopics += topics.size();

            previousYearCompletedTopics +=
                    (int) topics.stream()
                            .filter(Topic::isCompleted)
                            .count();
        }

        int attemptYearTotalTopics = 0;
        int attemptYearCompletedTopics = 0;

        for (Section section : attemptYearSections) {

            List<Topic> topics =
                    getTopicsForSection(section.getId());

            attemptYearTotalTopics += topics.size();

            attemptYearCompletedTopics +=
                    (int) topics.stream()
                            .filter(Topic::isCompleted)
                            .count();
        }

        int previousYearProgress =
                previousYearTotalTopics == 0
                        ? 0
                        : (int) (
                        (previousYearCompletedTopics * 100L)
                        / previousYearTotalTopics
                );

        int attemptYearProgress =
                attemptYearTotalTopics == 0
                        ? 0
                        : (int) (
                        (attemptYearCompletedTopics * 100L)
                        / attemptYearTotalTopics
                );

        model.addAttribute("attemptYear", attemptYear);
        model.addAttribute("previousYear", previousYear);
        model.addAttribute(
                "previousYearSections",
                previousYearSections
        );
        model.addAttribute(
                "attemptYearSections",
                attemptYearSections
        );
        model.addAttribute(
                "previousYearProgress",
                previousYearProgress
        );
        model.addAttribute(
                "attemptYearProgress",
                attemptYearProgress
        );

        return "prelims-current-affairs";
    }

    @GetMapping("/current-affairs/prelims/month/{sectionId}")
    public String openPrelimsMonth(
            @PathVariable("sectionId") Long sectionId,
            Model model,
            HttpSession session) {

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Section section =
                sectionRepository.findById(sectionId)
                        .orElseThrow();

        createPrelimsTopics(section);

        List<Topic> topics =
                getTopicsForSection(sectionId);

        int completedCount =
                (int) topics.stream()
                        .filter(Topic::isCompleted)
                        .count();

        int progress =
                topics.isEmpty()
                        ? 0
                        : (int) (
                        (completedCount * 100L)
                        / topics.size()
                );

        model.addAttribute("section", section);
        model.addAttribute("topics", topics);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("progress", progress);
        model.addAttribute("backAttemptYear", attemptYear);
        model.addAttribute("backType", "prelims");

        return "prelims-current-affairs-topics";
    }

    @PostMapping("/current-affairs/toggle/{topicId}")
    public String togglePrelimsTopic(
            @PathVariable("topicId") Long topicId,
            HttpSession session) {

        Topic topic =
                topicRepository.findById(topicId)
                        .orElseThrow();

        topic.setCompleted(!topic.isCompleted());

        topicRepository.save(topic);

        Section section = topic.getSection();

        if (section == null) {
            return "redirect:/current-affairs";
        }

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        return "redirect:/current-affairs/prelims/month/"
                + section.getId();
    }

    // =========================================================
    // MAINS CURRENT AFFAIRS
    // =========================================================

    @GetMapping("/current-affairs/mains")
    public String showMains(
            Model model,
            HttpSession session) {

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Subject subject =
                getCurrentAffairsSubject();

        String[] months = getMonths();

        List<Section> revisionSections =
                new ArrayList<>();

        int previousYear = attemptYear - 1;

        for (String month : months) {

            Section section =
                    getOrCreateCategorizedSection(
                            month,
                            previousYear,
                            subject,
                            "revision"
                    );

            revisionSections.add(section);
        }

        for (int i = 0; i < 5; i++) {

            Section section =
                    getOrCreateCategorizedSection(
                            months[i],
                            attemptYear,
                            subject,
                            "revision"
                    );

            revisionSections.add(section);
        }

        List<Section> mainsSections =
                new ArrayList<>();

        for (int i = 5; i < months.length; i++) {

            Section section =
                    getOrCreateCategorizedSection(
                            months[i],
                            attemptYear,
                            subject,
                            "mains"
                    );

            mainsSections.add(section);
        }

        model.addAttribute("attemptYear", attemptYear);
        model.addAttribute("revisionSections", revisionSections);
        model.addAttribute("mainsSections", mainsSections);

        return "mains-current-affairs";
    }

    @GetMapping("/current-affairs/mains/month/{sectionId}")
    public String openMainsMonth(
            @PathVariable("sectionId") Long sectionId,
            Model model,
            HttpSession session) {

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Section section =
                sectionRepository.findById(sectionId)
                        .orElseThrow();

        model.addAttribute("section", section);
        model.addAttribute("attemptYear", attemptYear);
        model.addAttribute("completed", section.isCompleted());

        return "mains-current-affairs-month";
    }

    @PostMapping("/current-affairs/mains/toggle/{sectionId}")
    public String toggleMainsMonth(
            @PathVariable("sectionId") Long sectionId,
            HttpSession session) {

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Section section =
                sectionRepository.findById(sectionId)
                        .orElseThrow();

        section.setCompleted(!section.isCompleted());

        sectionRepository.save(section);

        return "redirect:/current-affairs/mains";
    }

    // =========================================================
    // 365 DAYS CURRENT AFFAIRS
    // =========================================================

    @GetMapping("/current-affairs/365")
    public String show365(
            Model model,
            HttpSession session) {

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Subject subject =
                getCurrentAffairsSubject();

        String[] categories =
                get365Categories();

        List<Section> categories365 =
                new ArrayList<>();

        for (String categoryName : categories) {

            Section section =
                    getOrCreate365Category(
                            categoryName,
                            attemptYear,
                            subject
                    );

            categories365.add(section);
        }

        int totalCategories =
                categories365.size();

        int completedCategories =
                (int) categories365.stream()
                        .filter(Section::isCompleted)
                        .count();

        int progress =
                totalCategories == 0
                        ? 0
                        : (int) (
                        (completedCategories * 100L)
                        / totalCategories
                );

        model.addAttribute(
                "attemptYear",
                attemptYear
        );

        model.addAttribute(
                "categories365",
                categories365
        );

        model.addAttribute(
                "totalCategories",
                totalCategories
        );

        model.addAttribute(
                "completedCategories",
                completedCategories
        );

        model.addAttribute(
                "progress",
                progress
        );

        return "365-current-affairs";
    }

    // =========================================================
    // 365 DAYS - COMPLETE / INCOMPLETE
    // =========================================================

    @PostMapping(
            "/current-affairs/365/toggle/{sectionId}"
    )
    public String toggle365Category(
            @PathVariable("sectionId") Long sectionId,
            HttpSession session) {

        Integer attemptYear =
                getAttemptYearFromSession(session);

        if (attemptYear == null) {
            return "redirect:/settings?error=Please+select+your+UPSC+attempt+year+first";
        }

        Section section =
                sectionRepository.findById(sectionId)
                        .orElseThrow();

        if (!"365".equalsIgnoreCase(
                section.getCategory())) {

            return "redirect:/current-affairs/365";
        }

        if (section.getYear() == null ||
                !section.getYear().equals(attemptYear)) {

            return "redirect:/current-affairs/365";
        }

        section.setCompleted(
                !section.isCompleted()
        );

        sectionRepository.save(section);

        return "redirect:/current-affairs/365";
    }

    // =========================================================
    // 365 DAYS CATEGORIES
    // =========================================================

    private String[] get365Categories() {

        return new String[] {

                "Polity & Governance",

                "Economy",

                "Environment & Ecology",

                "Science & Technology",

                "International Relations",

                "Geography",

                "Society & Social Justice",

                "History, Art & Culture",

                "Agriculture & Rural Development",

                "Important Events & Miscellaneous"
        };
    }

    // =========================================================
    // CREATE / GET 365 CATEGORY
    // =========================================================

    private Section getOrCreate365Category(
            String name,
            int year,
            Subject subject) {

        List<Section> sections =
                sectionRepository.findBySubjectIdAndYear(
                        subject.getId(),
                        year
                );

        for (Section section : sections) {

            if (section.getName() != null &&
                    section.getName()
                            .equalsIgnoreCase(name)) {

                if (section.getCategory() == null ||
                        !section.getCategory()
                                .equalsIgnoreCase("365")) {

                    section.setCategory("365");

                    sectionRepository.save(section);
                }

                return section;
            }
        }

        Section section =
                new Section();

        section.setName(name);
        section.setYear(year);
        section.setCategory("365");
        section.setCompleted(false);
        section.setSubject(subject);

        return sectionRepository.save(section);
    }

    // =========================================================
    // PRELIMS TOPICS
    // =========================================================

    private void createPrelimsTopics(
            Section section) {

        String[] topicNames = {

                "Polity",

                "Governance",

                "Economy",

                "Environment",

                "International Relations",

                "Science and Technology",

                "Security",

                "Social Issues",

                "Art and Culture",

                "Geography",

                "Agriculture",

                "Reports and Indices",

                "Government Schemes",

                "Miscellaneous"
        };

        createTopics(
                section,
                topicNames
        );
    }

    private void createTopics(
            Section section,
            String[] topicNames) {

        if (section == null ||
                section.getId() == null) {

            return;
        }

        List<Topic> existingTopics =
                getTopicsForSection(
                        section.getId()
                );

        for (String topicName : topicNames) {

            boolean exists =
                    existingTopics.stream()
                            .anyMatch(topic ->
                                    topic.getTopicName() != null &&
                                            topic.getTopicName()
                                                    .equalsIgnoreCase(
                                                            topicName
                                                    )
                            );

            if (!exists) {

                Topic topic =
                        new Topic();

                topic.setTopicName(
                        topicName
                );

                topic.setCompleted(false);

                topic.setSubject(
                        section.getSubject()
                );

                topic.setSection(
                        section
                );

                topicRepository.save(topic);
            }
        }
    }

    // =========================================================
    // GET TOPICS
    // =========================================================

    private List<Topic> getTopicsForSection(
            Long sectionId) {

        if (sectionId == null) {
            return new ArrayList<>();
        }

        return topicRepository.findAll()
                .stream()
                .filter(topic ->
                        topic.getSection() != null &&
                                topic.getSection().getId() != null &&
                                topic.getSection()
                                        .getId()
                                        .equals(sectionId)
                )
                .toList();
    }

    // =========================================================
    // PRELIMS SECTION
    // =========================================================

    private Section getOrCreatePrelimsSection(
            String name,
            int year,
            Subject subject) {

        List<Section> sections =
                sectionRepository.findBySubjectIdAndYear(
                        subject.getId(),
                        year
                );

        for (Section section : sections) {

            if (section.getName() != null &&
                    section.getName()
                            .equalsIgnoreCase(name)) {

                if (section.getCategory() == null ||
                        !section.getCategory()
                                .equalsIgnoreCase("prelims")) {

                    section.setCategory("prelims");

                    sectionRepository.save(section);
                }

                return section;
            }
        }

        Section section =
                new Section();

        section.setName(name);
        section.setYear(year);
        section.setCategory("prelims");
        section.setCompleted(false);
        section.setSubject(subject);

        return sectionRepository.save(section);
    }

    // =========================================================
    // GENERIC CATEGORIZED SECTION
    // =========================================================

    private Section getOrCreateCategorizedSection(
            String name,
            int year,
            Subject subject,
            String category) {

        List<Section> sections =
                sectionRepository.findBySubjectIdAndYear(
                        subject.getId(),
                        year
                );

        for (Section section : sections) {

            if (section.getName() != null &&
                    section.getName()
                            .equalsIgnoreCase(name)) {

                boolean categoryDifferent =
                        section.getCategory() == null ||
                                !section.getCategory()
                                        .equalsIgnoreCase(category);

                if (categoryDifferent) {

                    section.setCategory(
                            category
                    );

                    sectionRepository.save(section);
                }

                return section;
            }
        }

        Section section =
                new Section();

        section.setName(name);
        section.setYear(year);
        section.setCategory(category);
        section.setCompleted(false);
        section.setSubject(subject);

        return sectionRepository.save(section);
    }

    // =========================================================
    // CURRENT AFFAIRS SUBJECT
    // =========================================================

    private Subject getCurrentAffairsSubject() {

        return subjectRepository
                .findById(8L)
                .orElseThrow();
    }

    // =========================================================
    // ATTEMPT YEAR
    // =========================================================

    private Integer getAttemptYearFromSession(
            HttpSession session) {

        Object value =
                session.getAttribute(
                        "attemptYear"
                );

        if (value instanceof Number) {

            return ((Number) value)
                    .intValue();
        }

        return null;
    }

    // =========================================================
    // MONTHS
    // =========================================================

    private String[] getMonths() {

        return new String[] {

                "January",

                "February",

                "March",

                "April",

                "May",

                "June",

                "July",

                "August",

                "September",

                "October",

                "November",

                "December"
        };
    }
}