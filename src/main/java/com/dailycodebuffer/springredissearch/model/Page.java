package com.dailycodebuffer.springredissearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Page {
    private List<Post> posts;
    private Integer totalPage;
    private Integer currentPage;
    private Long total;
}
