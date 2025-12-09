package com.cc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cc.DTO.NewsDTO;
import com.cc.modals.News;
import com.cc.repository.NewsRepository;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    // Create a new news item
    public NewsDTO createNews(NewsDTO newsDTO) {
        News news = new News(
                newsDTO.getActiveFrom(),
                newsDTO.getTillDate(),
                newsDTO.getHeading(),
                newsDTO.getContent(),
                newsDTO.getTag(),
                newsDTO.getUsername(),
                newsDTO.getForUser());

        News savedNews = newsRepository.save(news);
        return convertToDTO(savedNews);
    }

    // Get all news items
    public List<NewsDTO> getAllNews() {
        List<News> newsList = newsRepository.findByActiveOrderByCreatedAtDesc(true);
        return newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get news by ID
    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        return convertToDTO(news);
    }

    // Get currently active news (within date range)
    public List<NewsDTO> getCurrentlyActiveNews() {
        LocalDate today = LocalDate.now();
        List<News> newsList = newsRepository.findCurrentlyActiveNews(today);
        return newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get currently active news for a specific user type
    public List<NewsDTO> getCurrentlyActiveNewsForUser(String forUser) {
        LocalDate today = LocalDate.now();
        List<News> newsList = newsRepository.findCurrentlyActiveNewsForUser(forUser, today);
        return newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get news by tag
    public List<NewsDTO> getNewsByTag(String tag) {
        LocalDate today = LocalDate.now();
        List<News> newsList = newsRepository.findCurrentlyActiveNewsByTag(tag, today);
        return newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Get news created by a specific user
    public List<NewsDTO> getNewsByUsername(String username) {
        List<News> newsList = newsRepository.findByUsernameOrderByCreatedAtDesc(username);
        return newsList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Update news
    public NewsDTO updateNews(Long id, NewsDTO newsDTO) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        news.setActiveFrom(newsDTO.getActiveFrom());
        news.setTillDate(newsDTO.getTillDate());
        news.setHeading(newsDTO.getHeading());
        news.setContent(newsDTO.getContent());
        news.setTag(newsDTO.getTag());
        news.setForUser(newsDTO.getForUser());
        news.setUpdatedAt(LocalDateTime.now());

        News updatedNews = newsRepository.save(news);
        return convertToDTO(updatedNews);
    }

    // Soft delete news (set active to false)
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        news.setActive(false);
        news.setUpdatedAt(LocalDateTime.now());
        newsRepository.save(news);
    }

    // Hard delete news
    public void hardDeleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new RuntimeException("News not found with id: " + id);
        }
        newsRepository.deleteById(id);
    }

    // Convert News entity to DTO
    private NewsDTO convertToDTO(News news) {
        return new NewsDTO(
                news.getId(),
                news.getActiveFrom(),
                news.getTillDate(),
                news.getHeading(),
                news.getContent(),
                news.getTag(),
                news.getUsername(),
                news.getForUser(),
                news.getCreatedAt(),
                news.getUpdatedAt(),
                news.isActive());
    }
}
