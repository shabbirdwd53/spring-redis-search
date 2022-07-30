# Redis search using Spring Boot and Jedis

## Installing Redisearch and RedisJson

docker pull redis/redis-stack

Start redis and then in cli run : redis-cli monitor - this will help to watch what queries are executed.

## Topics covered 
Secondary indexing

Multi-field queries

Aggregation

Full-text indexing of multiple fields in a document

Stemming-based query expansion

Auto-complete suggestions

## Commands :
### FT.create -
This command is used to create index

Example of Index creation on hash : FT.CREATE idx:post ON hash PREFIX 1 "post:" SCHEMA content TEXT SORTABLE views NUMERIC SORTABLE tags TAG

Example of Index creation on JSON : FT.CREATE "post-idx" "ON" "JSON" "PREFIX" "1" "post:" "SCHEMA" "$.content" "AS" "content" "TEXT" "$.title" "AS" "title" "TEXT" "$.tags[*]" "AS" "tags" "TAG" "$.views" "AS" "views" "NUMERIC" "NOINDEX"

Note : Whenever we are dealing with RedisJson the attributes in index needs to be accessed with $.

Refer : https://redis.io/commands/ft.create/


### FT.search -
This command has capablity to search through index.It provides several capablity like Fuzzy search , text search , tag search.
Search can happen either on Set or Json.

Example of search command : "FT.SEARCH" "post-idx" "@content:tha @tags:{a}" "LIMIT" "0" "20"


In this example we are passing filter of content contains some word. This word gets for search and do stemming. Stemming mean searching similar word like if we pass loving , it could find love, loving , loved.
Then we did tag searching where it does actual tagging search.. You can consider filtering using category.

We also passed Limit , which helps to limit the data , by default limit is set to 10 which can be change by passing in query.

https://redis.io/commands/ft.search/

### FT.INFO -
This command will help to get the index information , what it is and what are attributes which are included.

### FT.AGGREGATE -
As the name suggest it helps to do aggregation functions like we do in sql. For example group by some field and do aggregation operations.

This query helps to run the reporting queries where we want to gather stats.

Example of command : FT.AGGREGATE "post-idx" "*" "GROUPBY" "1" "$tags" "REDUCE" "COUNT" "0" "AS" "NO_OF_POST" "REDUCE" "AVG" "1" "@views" "AS" "AVERAGE_VIEWS"

In this command we passed group by tags and then we have given multiple reducer. Reducers contains aggregation function like count,sum, avg.

In addition to this we can also pass filters in the command to aggregate on filtered data.

https://redis.io/commands/ft.aggregate/

### Other important commands
FT.DROPIDX - Dropping index
FT.SUGADD - Used to get suggestion added to dictionary
FT.SUGGET - Used to search suggestion in dictionary
https://redis.io/commands/ft.sugadd/
https://redis.io/commands/ft.sugget/

It is mainly used for autocomplete search.

## Running and Testing code :
Main Class : RedisSearchApplication.java , run as spring boot application

Below are two endpoints which can be tested :

http://localhost:8080/filter/?search=love - Searching based on search keyword . It also supports page and tag as query parameter.

http://localhost:8080/categoryWiseStats - Helps to get category wis statastics 



## Library used

To perform this operation we used Jedis library. Jedis client has support of both RediSearch and RediJson.

On starting of the server it will load data , create index in the redis.

Configure application properties with host and port of the redis.



##More Info
https://developer.redis.com/howtos/moviesdatabase/create/ 

Good Video : https://www.youtube.com/watch?v=krmY1547b3A 


