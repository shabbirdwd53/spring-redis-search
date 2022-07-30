package com.dailycodebuffer.springredissearch.service;

import com.dailycodebuffer.springredissearch.model.CategoryStats;
import com.dailycodebuffer.springredissearch.model.Page;
import com.dailycodebuffer.springredissearch.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public Page search(String content, Set<String> tags, Integer page) {
        return postRepository.search(content,tags,page);
    }

    public List<CategoryStats> getCategoryWiseTotalPost() {
        return postRepository.getCategoryWiseTotalPost();
    }
}
