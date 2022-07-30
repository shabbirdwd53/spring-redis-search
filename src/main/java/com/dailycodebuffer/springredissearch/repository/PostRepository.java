package com.dailycodebuffer.springredissearch.repository;

import com.dailycodebuffer.springredissearch.model.CategoryStats;
import com.dailycodebuffer.springredissearch.model.Page;
import com.dailycodebuffer.springredissearch.model.Post;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Reducers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Repository
public class PostRepository {

    @Autowired
    private UnifiedJedis jedis;

    private static final Integer PAGE_SIZE = 5;

    public Post save(Post p) {
        if(p.getPostId() ==  null) {
            p.setPostId(UUID.randomUUID().toString());
        }
        Gson gson = new Gson();
        String key = "post:" + p.getPostId();
        jedis.jsonSet(key, gson.toJson(p));
        jedis.sadd("post", key);
        return p;
    }

    public void deleteAll() {
        Set<String> keys = jedis.smembers("post");
        if(!keys.isEmpty()) {
            keys.stream()
                    .forEach(jedis::jsonDel);
        }
        jedis.del("post");
    }

    public Page search(String content, Set<String> tags, Integer page) {
        Long totalResults = 0l;

        StringBuilder queryBuilder = new StringBuilder();

        if(content !=null && !content.isEmpty()) {
            queryBuilder.append("@content:" + content);
        }

        if(tags !=null && !tags.isEmpty()) {
            queryBuilder.append(" @tags:{" +
                    tags.stream().collect(Collectors.joining("|"))
                    + "}");
        }

        String queryCriteria = queryBuilder.toString();
        Query query = null;

        if(queryCriteria.isEmpty()) {
            query = new Query();
        } else {
            query = new Query(queryCriteria);
        }

        query.limit(PAGE_SIZE * (page-1), PAGE_SIZE);
        SearchResult searchResult
                = jedis.ftSearch("post-idx",query);
        totalResults = searchResult.getTotalResults();
        int numberOfPages =
                (int) Math.ceil((double)totalResults/PAGE_SIZE);

        List<Post> postList = searchResult.getDocuments()
                .stream()
                .map(this::convertDocumentToPost)
                .collect(Collectors.toList());

        return Page.builder()
                .posts(postList)
                .total(totalResults)
                .totalPage(numberOfPages)
                .currentPage(page)
                .build();
    }

    private Post convertDocumentToPost(Document document) {
        Gson gson = new Gson();
        String jsonDoc = document
                .getProperties()
                .iterator()
                .next()
                .getValue()
                .toString();
        return gson.fromJson(jsonDoc,Post.class);
    }

    public List<CategoryStats> getCategoryWiseTotalPost() {
        AggregationBuilder aggregationBuilder
                = new AggregationBuilder();
        aggregationBuilder.groupBy("@tags",
                Reducers.count().as("NO_OF_POST"),
                Reducers.avg("@views").as("AVERAGE_VIEWS"));

        AggregationResult aggregationResult
                = jedis.ftAggregate("post-idx",aggregationBuilder);

        List<CategoryStats> categoryStatsList = new ArrayList<>();

        LongStream.range(0, aggregationResult.totalResults)
                .mapToObj(idx -> aggregationResult.getRow((int) idx))
                .forEach(row -> {
                    categoryStatsList.add(
                            CategoryStats.builder()
                                    .totalPosts(row.getLong("NO_OF_POST"))
                                    .averageViews(new DecimalFormat("#.##")
                                            .format(row.getDouble("AVERAGE_VIEWS")))
                                    .tags(row.getString("tags"))
                                    .build()
                    );
                });
        return categoryStatsList;
    }
}
