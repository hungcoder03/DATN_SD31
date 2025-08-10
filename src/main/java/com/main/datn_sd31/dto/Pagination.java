package com.main.datn_sd31.dto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class Pagination<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean isLast;

    public Pagination() {}

    public Pagination(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.pageSize = page.getSize();
        this.isLast = page.isLast();
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    // Các getter bổ sung để tương thích với Thymeleaf (giống Page của Spring)
    public int getNumber() {
        return this.currentPage;
    }

    public int getSize() {
        return this.pageSize;
    }

    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }

    public boolean isFirst() {
        return this.currentPage == 0;
    }
}