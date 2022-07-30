package com.dailycodebuffer.springredissearch.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Post {
    private String postId;
    private String content;
    private String title;
    private Set<String> tags = new HashSet<>();
    private Integer views;
}
