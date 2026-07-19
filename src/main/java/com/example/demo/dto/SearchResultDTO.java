package com.example.demo.dto;

import java.util.List;

public class SearchResultDTO<T> {
    private List<T> items;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public SearchResultDTO() {
    }

    public SearchResultDTO(List<T> items, long totalElements, int totalPages, int currentPage) {
        this.items = items;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
