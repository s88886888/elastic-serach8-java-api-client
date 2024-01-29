//package com.hotel;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.SortOrder;
//import co.elastic.clients.elasticsearch.core.*;
//import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import co.elastic.clients.elasticsearch.core.search.TotalHits;
//import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
//import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
//import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
//import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.ElasticsearchTransport;
//import co.elastic.clients.transport.endpoints.BooleanResponse;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import com.spingcould.es8.pojo.Hotel;
//import jakarta.annotation.Resource;
//import org.apache.http.Header;
//import org.apache.http.HttpHost;
//import org.apache.http.message.BasicHeader;
//import org.elasticsearch.client.RestClient;
//import org.junit.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@SpringBootTest
//public class HotelEs8ApplicationTests {
//    @Resource
//    private ElasticsearchClient client;
//
//    @Test
//    public void contextLoads() throws IOException {
//
//        // URL and API key
//        String serverUrl = "https://192.168.1.19:9200";
//        String apiKey = "STdsZFFvd0JyVGdIckVyUEpLNHo6VkE0Zm9KZl9Rcnlybm9sTFJmaEw5dw==";
//
//// Create the low-level client
//        RestClient restClient = RestClient
//                .builder(HttpHost.create(serverUrl))
//                .setDefaultHeaders(new Header[]{
//                        new BasicHeader("Authorization", "ApiKey " + apiKey)
//                })
//                .build();
//
//// Create the transport with a Jackson mapper
//        ElasticsearchTransport transport = new RestClientTransport(
//                restClient, new JacksonJsonpMapper());
//
//// And create the API client
//        ElasticsearchClient esClient = new ElasticsearchClient(transport);
//
//    }
//
//
//    //创建索引
//    @Test
//    public void createIndex() throws IOException {
//        //写法比RestHighLevelClient更加简洁
//        CreateIndexResponse indexResponse = client.indices().create(c -> c.index("hotel"));
//        System.err.println(indexResponse.acknowledged());
//
//    }
//
//    //查询索引
//    @Test
//    public void queryIndex() throws IOException {
//
//        //判断索引是否存在？
//        BooleanResponse booleanResponse = client.indices().exists(e -> e.index("hotel"));
//        System.err.println(booleanResponse.value());
//        if (booleanResponse.value()) {
//            //查询索引
//            GetIndexResponse getIndexResponse = client.indices().get(i -> i.index("hotel"));
//            System.err.println(getIndexResponse.result().get("hotel"));
//        }
//    }
//
//    //删除索引
//    @Test
//    public void deleteIndex() throws IOException {
//        DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index("hotel"));
//        System.err.println(deleteIndexResponse.acknowledged());
//    }
//
//    //根据索引插入单条文档
//    @Test
//    public void insertDocument() throws IOException {
//        IndexResponse indexResponse = client.index(i -> i
//                .index("hotel")
//                .id(new Hotel(5L, "linson").getId().toString())
//                .document(new Hotel(5L, "linson"))
//        );
//        System.err.println(indexResponse.version());
//    }
//
//    //批量插入文档
//    @Test
//    public void insertDocumentList() throws IOException {
//        List<Hotel> hotelList = new ArrayList<>();
//// 添加酒店对象到列表中
//        hotelList.add(new Hotel(1L, "linson C"));
//        hotelList.add(new Hotel(4L, "linson A"));
//        hotelList.add(new Hotel(2L, "linson B"));
//        hotelList.add(new Hotel(3L, "linson C"));
//        hotelList.add(new Hotel(5L, "linson QAQ"));
//
//        BulkRequest.Builder br = new BulkRequest.Builder();
//
//        //遍历添加到bulk中
//        for (Hotel hotel : hotelList) {
//            br.operations(op -> op
//                    .index(idx -> idx
//                            .index("hotel")
//                            .id(hotel.getId().toString())
//                            .document(hotel)
//                    )
//
//            );
//
//        }
//
//        BulkResponse result = client.bulk(br.build());
//
//        if (result.errors()) {
//            System.err.println("Bulk had errors");
//            for (BulkResponseItem item : result.items()) {
//                if (item.error() != null) {
//                    System.err.println(item.error().reason());
//                }
//            }
//        }
//
//    }
//
//    //根据索引更新文档
//    @Test
//    public void updateDocumentTest() throws IOException {
//        UpdateResponse<Hotel> updateResponse = client.update(u -> u
//                        .index("hotel")
//                        .id("5")
//                        .doc(new Hotel(5L, "linson QAQ5"))
//                , Hotel.class);
//    }
//
//    //判断文档是否存在
//    @Test
//    public void existDocumentTest() throws IOException {
//        BooleanResponse indexResponse = client.exists(e -> e.index("hotel").id("1"));
//        System.out.println(indexResponse.value());
//    }
//
//    //查询文档根据id
//    @Test
//    public void getDocumentTest() throws IOException {
//        GetResponse<Hotel> getResponse = client.get(g -> g
//                        .index("hotel")
//                        .id("1")
//                , Hotel.class
//        );
//        System.out.println(getResponse.source());
//    }
//
//    //搜索文档根据name
//    @Test
//    public void searchDocumentTest() throws IOException {
//
//        String searchText = "希尔顿";
//
//        SearchResponse<Hotel> response = client.search(s -> s
//                        .index("hotel")  // 设置要搜索的索引名称为 "hotel"
//                        //去除query 则是查询全部
//                        .query(q -> q
//                                .match(t -> t
//                                        .field("name")  // 设置要匹配的字段为 "name"
//                                        .analyzer("ik_max_word")
//                                        .query(searchText)  // 设置匹配的搜索文本为变量 "searchText"
//
//                                )
//                        )
//
//                        .sort(x -> x.field(z -> z.field("id").order(SortOrder.Asc)))  // 设置排序规则，按 id 升序排序
//                        .from(2)  // 设置结果集的起始位置，从第 0 条开始
//                        .size(10)  // 设置每页返回的结果数量为 10 条
//                        .highlight(x -> x
//                                .preTags("<font color='red'>")
//                                .postTags("</font>")
//                                .fields("name", z -> z)) // 高亮显示匹配的字段 "name"，并设置前缀和后缀
//                , Hotel.class  // 指定返回结果的数据类型为 Hotel 类
//        );
//
//        TotalHits total = response.hits().total();
//        boolean isExactResult = false;
//        if (total != null) {
//            isExactResult = total.relation() == TotalHitsRelation.Eq;
//        }
//        if (isExactResult) {
//            System.err.println("There are " + total.value() + " results");
//        } else {
//            if (total != null) {
//                System.err.println("There are more than " + total.value() + " results");
//            }
//        }
//
////        System.err.println(response.hits().hits());
//
//        List<Hit<Hotel>> hits = response.hits().hits();
//        for (Hit<Hotel> hit : hits) {
//            Hotel hotel = hit.source();
//            System.err.println(hit.highlight().get("name"));
//
//            if (hotel != null) {
//                System.err.println("Found  " + hotel.getName());
//            }
//        }
//
//    }
//
//
//}
