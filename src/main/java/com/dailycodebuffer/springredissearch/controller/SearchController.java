package com.dailycodebuffer.springredissearch.controller;

import com.dailycodebuffer.springredissearch.model.CategoryStats;
import com.dailycodebuffer.springredissearch.model.Page;
import com.dailycodebuffer.springredissearch.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/")
public class SearchController {

    @Autowired
    private PostService postService;

    @GetMapping("/search")
    public Page search(@RequestParam(name = "content", required = false) String content,
                       @RequestParam(name = "tags", required = false) Set<String> tags,
                       @RequestParam(name = "page", defaultValue = "1") Integer page) {
        return postService.search(content,tags,page);
    }

    @GetMapping("/categoryWisePost")
    public List<CategoryStats> getCategoryWiseTotalPost() {
        return postService.getCategoryWiseTotalPost();
    }
}
