package com.example.annita.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
@Setter
public class PageResponse<T> {

    private List<T> data;
    private Meta meta;

    public PageResponse(Page<?> page, List<T> data) {
        this.data = data;
        this.meta = new Meta(page);
    }

    @Getter
    @Setter
    public static class Meta {
        private int page;
        private int perPage;
        private long totalElements;
        private int totalPages;

        public Meta(Page<?> page) {
            this.page = page.getNumber();
            this.perPage = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
        }
    }
}
