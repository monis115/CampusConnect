package com.cc.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cc.DTO.NewsDTO;
import com.cc.service.NewsService;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * Create a new news item
     * POST /api/news
     * Request body: {
     * "activeFrom": "2024-01-01",
     * "tillDate": "2024-12-31",
     * "heading": "News Heading",
     * "content": "News content...",
     * "tag": "announcement",
     * "username": "admin",
     * "forUser": "AGENT"
     * }
     */
    @PostMapping
    public ResponseEntity<NewsDTO> createNews(@RequestBody NewsDTO newsDTO) {
        try {
            NewsDTO createdNews = newsService.createNews(newsDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all active news items
     * GET /api/news
     */
    @GetMapping
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        List<NewsDTO> newsList = newsService.getAllNews();
        return ResponseEntity.ok(newsList);
    }

    /**
     * Get a specific news item by ID
     * GET /api/news/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        try {
            NewsDTO news = newsService.getNewsById(id);
            return ResponseEntity.ok(news);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get currently active news (within date range)
     * GET /api/news/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<NewsDTO>> getCurrentlyActiveNews() {
        List<NewsDTO> newsList = newsService.getCurrentlyActiveNews();
        return ResponseEntity.ok(newsList);
    }

    /**
     * Get active news for a specific user type
     * GET /api/news/for-user/{forUser}
     * Example: /api/news/for-user/AGENT
     */
    @GetMapping("/for-user/{forUser}")
    public ResponseEntity<List<NewsDTO>> getNewsForUser(@PathVariable String forUser) {
        List<NewsDTO> newsList = newsService.getCurrentlyActiveNewsForUser(forUser);
        return ResponseEntity.ok(newsList);
    }

    /**
     * Get news by tag
     * GET /api/news/tag/{tag}
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<NewsDTO>> getNewsByTag(@PathVariable String tag) {
        List<NewsDTO> newsList = newsService.getNewsByTag(tag);
        return ResponseEntity.ok(newsList);
    }

    /**
     * Get news created by a specific user
     * GET /api/news/by-user/{username}
     */
    @GetMapping("/by-user/{username}")
    public ResponseEntity<List<NewsDTO>> getNewsByUsername(@PathVariable String username) {
        List<NewsDTO> newsList = newsService.getNewsByUsername(username);
        return ResponseEntity.ok(newsList);
    }

    /**
     * Update a news item
     * PUT /api/news/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<NewsDTO> updateNews(@PathVariable Long id, @RequestBody NewsDTO newsDTO) {
        try {
            NewsDTO updatedNews = newsService.updateNews(id, newsDTO);
            return ResponseEntity.ok(updatedNews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Soft delete a news item (sets active to false)
     * DELETE /api/news/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNews(@PathVariable Long id) {
        try {
            newsService.deleteNews(id);
            return ResponseEntity.ok(Map.of("message", "News deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hard delete a news item (permanently removes from database)
     * DELETE /api/news/hard/{id}
     */
    @DeleteMapping("/hard/{id}")
    public ResponseEntity<Map<String, String>> hardDeleteNews(@PathVariable Long id) {
        try {
            newsService.hardDeleteNews(id);
            return ResponseEntity.ok(Map.of("message", "News permanently deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
