package com.example.demo.dto;

import java.util.List;

public class PageResponse<T> {
    private int code;
    private String message;
    private Data<T> data;

    public PageResponse(int code, String message, Data<T> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data<T> getData() {
        return data;
    }

    public void setData(Data<T> data) {
        this.data = data;
    }

    public static class Data<T> {
        private long total;
        private int page;
        private int pageSize;
        private int totalPages;
        private List<T> list;

        public Data(long total, int page, int pageSize, List<T> list) {
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
            this.list = list;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }
    }

    public static <T> PageResponse<T> success(long total, int page, int pageSize, List<T> list) {
        return new PageResponse<>(200, "success", new Data<>(total, page, pageSize, list));
    }
}
