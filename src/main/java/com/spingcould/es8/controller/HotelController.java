package com.spingcould.es8.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.GeoDistanceQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.mysql.cj.util.StringUtils;
import com.spingcould.es8.pojo.Hotel;
import com.spingcould.es8.pojo.HotelDoc;
import com.spingcould.es8.pojo.RestfulPage;
import com.spingcould.es8.service.IHotelService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


/**
 * @author linson
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {


    @Resource
    private IHotelService hotelService;

    @Resource
    private ElasticsearchClient client;


    /////////////////////////////////////////更多具体实现，看测试用例test文件夹 ==》HotelEs8ApplicationTests //////////////////////////////////////////
    /////////////////////////////////////////        条件不写死，嵌套查询请看list方法  //////////////////////////////////////////

    /**
     * 分页排序查询
     */
    @GetMapping("/getListAll")
    public RestfulPage getList(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false, value = "sortBy") String sort) throws IOException {
        RestfulPage restfulPage = new RestfulPage();
        page = Math.max(page - 1, 0);
        if (StringUtils.isNullOrEmpty(sort)) {
            sort = "id";
        }
        Integer finalPage = page;
        String finalSort = sort;
        SearchResponse<HotelDoc> response = client.search(s -> s
                        .index("hotel")  // 设置要搜索的索引名称为 "hotel"
                        //去除query 则是查询全部
                        .query(q -> q.matchAll(x -> x)

                        )
                        .sort(x -> x.field(z -> z.field(finalSort).order(SortOrder.Desc)))
                        .from(finalPage * size)
                        .size(size)
                , HotelDoc.class
        );

        TotalHits total = response.hits().total();
        if (total != null) {
            restfulPage.setTotal(total.value());
        }

        List<Hit<HotelDoc>> hits = response.hits().hits();
        List<HotelDoc> list = new ArrayList<>();
        for (Hit<HotelDoc> hit : hits) {
            HotelDoc hotel = hit.source();
            System.err.println(hit.highlight().get("name"));

            if (hotel != null) {
                System.err.println("Found  " + hotel.getName());
            }
            list.add(hotel);
        }
        restfulPage.setHotels(list);
        return restfulPage;
    }


    //分页查询 根据name使用分词器进行分词 page
    @GetMapping("/list1")
    public RestfulPage getList(@RequestParam String key, @RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false, value = "sortBy") String sort) throws IOException {
        RestfulPage restfulPage = new RestfulPage();
        page = Math.max(page - 1, 0);
        if (StringUtils.isNullOrEmpty(sort)) {
            sort = "id";
        }
        Integer finalPage = page;
        String finalSort = sort;
        SearchResponse<HotelDoc> response = client.search(s -> s
                        .index("hotel")  // 设置要搜索的索引名称为 "hotel"
                        //去除query 则是查询全部
                        .query(q -> q
                                .match(t -> t
                                        .field("name")  // 设置要匹配的字段为 "name"
                                        .query(c -> c.stringValue(key))  // 设置匹配的搜索文本为变量 "searchText"
                                        .analyzer("ik_max_word")  // 设置分词器要和索引库中的"name" 字段的分词器保持一致。"ik_smart","ik_max_word"
                                )

                        )
                        .sort(x -> x.field(z -> z.field(finalSort).order(SortOrder.Desc)))  // 设置排序规则，按 finalSort 顺序排序。重点：分页不设置排序，会出现数据重复
                        .from(finalPage * size)  // 设置结果集的起始位置，从第 0 条开始返回，【不是】从第几页开始返回
                        .size(size)  // 设置每页返回的结果数量为 10 条
                        .highlight(x -> x
                                .preTags("<font color='red'>")
                                .postTags("</font>")
                                .fields("name", z -> z)) // 高亮显示匹配的字段 "name"，并设置前缀和后缀
                , HotelDoc.class  // 指定返回结果的数据类型为 Hotel 类
        );
        TotalHits total = response.hits().total();
        if (total != null) {
            restfulPage.setTotal(total.value());
        }
        restfulPage.setHotels(null);
        boolean isExactResult = false;
        if (total != null) {
            isExactResult = total.relation() == TotalHitsRelation.Eq;
        }
        if (isExactResult) {
            System.err.println("There are " + total.value() + " results");
        } else {
            if (total != null) {
                System.err.println("There are more than " + total.value() + " results");
            }
        }
//        System.err.println(response.hits().hits());
        List<Hit<HotelDoc>> hits = response.hits().hits();
        for (Hit<HotelDoc> hit : hits) {
            HotelDoc hotel = hit.source();
            System.err.println(hit.highlight().get("name"));

            if (hotel != null) {
                System.err.println("Found  " + hotel.getName());
            }
        }
        return restfulPage;
    }


    //分页查询  根据name使用分词器进行分词 地理位置查询
    @GetMapping("/list2")
    public RestfulPage getList(@RequestParam String key, @RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false, value = "sortBy") String sort, @RequestParam(required = false, value = "location") String location) throws IOException {


        RestfulPage restfulPage = new RestfulPage();
        page = Math.max(page - 1, 0);
        if (StringUtils.isNullOrEmpty(sort)) {
            sort = "id";
        }
        System.err.println(location);
        if (StringUtils.isNullOrEmpty(location) || "undefined".equals(location)) {
            return new RestfulPage();
        }

        String finalSort = sort;
        Integer finalPage = page;
        SearchResponse<HotelDoc> response = client.search(s -> s
                        .index("hotel")  // 设置要搜索的索引名称为 "hotel"
                        //去除query 则是查询全部
                        .query(q -> q.bool(z -> z.must(m -> m.match(t ->
                                                        t.field("name")  // 设置要匹配的字段为 "name"
                                                                .query(key)  // 设置匹配的搜索文本为变量 "searchText"
                                                                .analyzer("ik_max_word")  // 设置分词器要和索引库中的"name" 字段的分词器保持一致。"ik_smart","ik_max_word"
                                                )
                                        ).filter(l -> l.geoDistance(x -> x.field("location").distance("30km")
                                                .location(r -> r.latlon(k -> k.lat(Double.parseDouble(location.split(",")[0]))
                                                        .lon(Double.parseDouble(location.split(",")[1]))))
                                        ))
                                )
                        )
                        .sort(x -> x.field(z -> z.field(finalSort).order(SortOrder.Desc)))  // 设置排序规则，按 finalSort 顺序排序。重点：分页不设置排序，会出现数据重复
                        .from(finalPage * size)  // 设置结果集的起始位置，从第 0 条开始返回，【不是】从第几页开始返回
                        .size(size)  // 设置每页返回的结果数量为 10 条
                        .highlight(x -> x
                                .preTags("<font color='red'>")
                                .postTags("</font>")
                                .fields("name", z -> z)) // 高亮显示匹配的字段 "name"，并设置前缀和后缀
                , HotelDoc.class  // 指定返回结果的数据类型为 Hotel 类
        );

        TotalHits total = response.hits().total();
        if (total != null) {
            restfulPage.setTotal(total.value());
        }

        boolean isExactResult = false;
        if (total != null) {
            isExactResult = total.relation() == TotalHitsRelation.Eq;
        }
        if (isExactResult) {
            System.err.println("There are " + total.value() + " results");
        } else {
            if (total != null) {
                System.err.println("There are more than " + total.value() + " results");
            }
        }
        System.err.println(response);
        List<Hit<HotelDoc>> hits = response.hits().hits();
        List<HotelDoc> list = new ArrayList<>();
        for (Hit<HotelDoc> hit : hits) {
            HotelDoc hotel = hit.source();
            System.err.println(hit.highlight().get("name"));
            if (hotel != null) {

                String output = hit.highlight().get("name").toString().substring(1, hit.highlight().get("name").toString().length() - 1);
                hotel.setName(output);
            }
            if (hotel != null) {
                System.err.println("Found  " + hotel.getName());
            }
            list.add(hotel);
        }
        restfulPage.setHotels(list);
        return restfulPage;

    }


    /**
     * 使用查询把条件整合到一个查询中
     */

    @GetMapping("/list")
    public RestfulPage getList(@RequestParam(required = false, value = "key") String key,
                               @RequestParam(required = false, value = "location") String location,
                               @RequestParam(required = false, defaultValue = "0", value = "page") Integer page,
                               @RequestParam(required = false, defaultValue = "5", value = "size") Integer size,
                               @RequestParam(required = false, defaultValue = "id", value = "sortBy") String sort) throws IOException {


        List<Query> queryBuilders = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(key)) {
            Query queryKey = MatchQuery.of(m -> m
                    .field("name")
                    .query(key)
            )._toQuery();
            queryBuilders.add(queryKey);
        }
        if (!StringUtils.isNullOrEmpty(location)) {
            Query queryLocation = GeoDistanceQuery.of(m -> m
                    .field("location")
                    .distance("30km")
                    .location(r -> r.latlon(k -> k.lat(Double.parseDouble(location.split(",")[0]))
                            .lon(Double.parseDouble(location.split(",")[1]))))
            )._toQuery();
            queryBuilders.add(queryLocation);
        }

        page = Math.max(page - 1, 0);
        Integer finalPage = page;
        SearchResponse<HotelDoc> response = client.search(s -> s
                        .index("hotel")
                        .query(q -> q.bool(b -> b.must(queryBuilders)))
                        .sort(x -> x.field(z -> z.field(sort).order(SortOrder.Desc)))
                        .from(finalPage * size)
                        .size(size)
                        .highlight(x -> x
                                .preTags("<font color='red'>")
                                .postTags("</font>")
                                .fields("name", z -> z)),
                HotelDoc.class);

        List<Hit<HotelDoc>> hits = response.hits().hits();
        List<HotelDoc> list = new ArrayList<>();
        for (Hit<HotelDoc> hit : hits) {
            HotelDoc hotel = hit.source();
            if (hotel != null) {
                if (!hit.highlight().isEmpty()) {
                    String output = hit.highlight().get("name").toString().substring(1, hit.highlight().get("name").toString().length() - 1);
                    hotel.setName(output);
                }
                list.add(hotel);
            }
        }
        RestfulPage restfulPage = new RestfulPage();
        TotalHits total = response.hits().total();
        if (total != null) {
            restfulPage.setTotal(total.value());
        }
        restfulPage.setHotels(list);
        return restfulPage;
    }


    /**
     *
     * 过滤器，过滤搜索中的关键词
     * GET /hotel/_search
     * {
     * "size": 0,
     * "aggs": {
     * "cities": {
     * "terms": {
     * "field": "city"
     * }
     * },
     * "brands": {
     * "terms": {
     * "field": "brand"
     * }
     * },
     * "starNames": {
     * "terms": {
     * "field": "starName"
     * }
     * }
     * }
     * }
     */
    @GetMapping("/ftilerList")
    public RestfulPage ftilerList() {

        return new RestfulPage();

    }


    //1.创建索引,设置映射，数据结构，搜索方式，设置集群、索引或节点的行为和属性
    @GetMapping("/addIndex")
    public void createIndex() {


        ///8.9 官方文档
        try {
            // 读取模板文件
            File file = new File("C:\\Users\\Administrator\\Desktop\\spring-colud-demo\\es8Service\\src\\main\\resources\\templates\\hotel.json");
            InputStream inputStream = Files.newInputStream(file.toPath());

            BooleanResponse isExists = client.indices().exists(
                    new ExistsRequest.Builder()
                            .index("hotel")
                            .build()
            );
            if (!isExists.value()) {
                CreateIndexResponse indexResponse = client.indices()
                        .create(c -> c.index("hotel").withJson(inputStream));
                System.err.println(indexResponse.acknowledged());
            }

        } catch (IOException e) {
            // 处理文件读取或创建索引时的异常
            e.printStackTrace();
        }
    }


    //2.为对应的索引填充数据 批量插入数据
    @GetMapping("/add")
    public RestfulPage addList() throws IOException {
        RestfulPage restfulPage = new RestfulPage();

        BooleanResponse isExists = client.indices().exists(
                new ExistsRequest.Builder()
                        .index("hotel")
                        .build()
        );

        if (isExists.value()) {
            BulkRequest.Builder br = new BulkRequest.Builder();
            //遍历添加到bulk中
            for (Hotel hotel : hotelService.list()) {
                HotelDoc hotelDoc = new HotelDoc(hotel);
                br.operations(op -> op
                        .index(idx -> idx
                                .index("hotel")
                                .id(hotel.getId().toString())
                                .document(hotelDoc)
                        )
                );
            }
            BulkResponse result = client.bulk(br.build());

            if (result.errors()) {
                System.err.println("Bulk had errors");
                for (BulkResponseItem item : result.items()) {
                    if (item.error() != null) {
                        System.err.println(item.error().reason());
                    }
                }
            }
        }
//        List<Hotel> hotels = hotelService.list();
//        List<HotelDoc> hotelDocs = new ArrayList<>();
//        for (Hotel hotel : hotels) {
//            hotelDocs.add(new HotelDoc(hotel));
//        }
//        restfulPage.setHotels(hotelDocs);
        return restfulPage;

    }


}
