package com.interview.coach.service;

import com.interview.coach.domain.ExamType;
import com.interview.coach.domain.Question;
import com.interview.coach.repository.QuestionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Optional<Question> randomQuestion(Integer year, ExamType type) {
        List<Question> candidates = questionRepository.findByFilters(year, type);
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
    }

    @Cacheable("questionYears")
    public List<Integer> years() {
        return questionRepository.findYears();
    }

    public Question getQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在或已下架"));
    }
}
