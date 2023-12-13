package com.hotel.controller;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
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
import com.hotel.pojo.Hotel;
import com.hotel.pojo.HotelDoc;
import com.hotel.pojo.RestfulPage;
import com.hotel.service.IHotelService;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author linson
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {


    @Autowired
    private IHotelService hotelService;

    @Autowired
    private ElasticsearchClient client;


    /////////////////////////////////////////更多具体实现，看测试用例test文件夹 ==》HotelEs8ApplicationTests //////////////////////////////////////////
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
        restfulPage.setHotels(response.hits().hits().stream().map(Hit::source).toList());
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
    @GetMapping("/list")
    public RestfulPage getList(@RequestParam String key, @RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false, value = "sortBy") String sort, @RequestParam(required = false, value = "location") String location) throws IOException {


        RestfulPage restfulPage = new RestfulPage();
        page = Math.max(page - 1, 0);
        if (StringUtils.isNullOrEmpty(sort)) {
            sort = "id";
        }
        System.err.println(location);
        if (StringUtils.isNullOrEmpty(location) || "undefined".equals(location)) {
            location = "31.1,121.1";
        }

        String finalSort = sort;
        Integer finalPage = page;
        String finalLocation = location;
        SearchResponse<HotelDoc> response = client.search(s -> s
                        .index("hotel")  // 设置要搜索的索引名称为 "hotel"
                        //去除query 则是查询全部
                        .query(q -> q.bool(z -> z.must(m -> m.match(t ->
                                                        t.field("name")  // 设置要匹配的字段为 "name"
                                                                .query(key)  // 设置匹配的搜索文本为变量 "searchText"
                                                                .analyzer("ik_max_word")  // 设置分词器要和索引库中的"name" 字段的分词器保持一致。"ik_smart","ik_max_word"
                                                )
                                        ).filter(l -> l.geoDistance(x -> x.field("location").distance("30km")
                                                .location(r -> r.latlon(k -> k.lat(Double.parseDouble(finalLocation.split(",")[0]))
                                                        .lon(Double.parseDouble(finalLocation.split(",")[1]))))
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
        }
        restfulPage.setHotels(hits.stream().map(Hit::source).toList());
        return restfulPage;

    }


    //1.创建索引,设置映射，数据结构，搜索方式，设置集群、索引或节点的行为和属性
    @GetMapping("/addIndex")
    public void createIndex() {

        try {
            // 读取模板文件
            File file = new File("C:\\Users\\Administrator\\Desktop\\hotel-es8\\src\\main\\resources\\templates\\hotel.json");
            InputStream inputStream = new FileInputStream(file);
            //写法比RestHighLevelClient更加简洁
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
//        restfulPage.setHotels(hotelService.list());
        return restfulPage;

    }


}
