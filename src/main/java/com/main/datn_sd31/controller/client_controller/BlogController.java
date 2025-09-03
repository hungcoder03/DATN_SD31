package com.main.datn_sd31.controller.client_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/blog")
public class BlogController {

    // Dữ liệu mẫu cho blog posts
    private final List<BlogPost> blogPosts = Arrays.asList(
        new BlogPost(1L, "blog.post1.title", "blog.post1.excerpt", "blog.post1.content", LocalDate.of(2024, 1, 15), "blog.post1.image"),
        new BlogPost(2L, "blog.post2.title", "blog.post2.excerpt", "blog.post2.content", LocalDate.of(2024, 1, 10), "blog.post2.image"),
        new BlogPost(3L, "blog.post3.title", "blog.post3.excerpt", "blog.post3.content", LocalDate.of(2024, 1, 5), "blog.post3.image"),
        new BlogPost(4L, "blog.post4.title", "blog.post4.excerpt", "blog.post4.content", LocalDate.of(2024, 1, 1), "blog.post4.image"),
        new BlogPost(5L, "blog.post5.title", "blog.post5.excerpt", "blog.post5.content", LocalDate.of(2023, 12, 25), "blog.post5.image")
    );

    @GetMapping("")
    public String blogList(Model model) {
        model.addAttribute("blogPosts", blogPosts);
        model.addAttribute("activePage", "blog");
        return "client/pages/blog/list";
    }

    @GetMapping("/{id}")
    public String blogDetail(@PathVariable Long id, Model model) {
        BlogPost post = blogPosts.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (post == null) {
            return "redirect:/blog";
        }
        
        model.addAttribute("post", post);
        model.addAttribute("activePage", "blog");
        return "client/pages/blog/detail";
    }

    // Inner class để lưu thông tin blog post
    public static class BlogPost {
        private Long id;
        private String titleKey;
        private String excerptKey;
        private String contentKey;
        private LocalDate publishDate;
        private String imageKey;

        public BlogPost(Long id, String titleKey, String excerptKey, String contentKey, LocalDate publishDate, String imageKey) {
            this.id = id;
            this.titleKey = titleKey;
            this.excerptKey = excerptKey;
            this.contentKey = contentKey;
            this.publishDate = publishDate;
            this.imageKey = imageKey;
        }

        // Getters
        public Long getId() { return id; }
        public String getTitleKey() { return titleKey; }
        public String getExcerptKey() { return excerptKey; }
        public String getContentKey() { return contentKey; }
        public LocalDate getPublishDate() { return publishDate; }
        public String getImageKey() { return imageKey; }
    }
} 