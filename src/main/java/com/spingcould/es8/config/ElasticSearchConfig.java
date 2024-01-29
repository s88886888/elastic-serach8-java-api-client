package com.spingcould.es8.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 */
@Configuration
public class ElasticSearchConfig {

//    //注入IOC容器
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        RestClient client = RestClient.builder(new HttpHost("192.168.1.19", 9200, "http")).build();
//        ElasticsearchTransport transport = new RestClientTransport(client, new JacksonJsonpMapper());
//        System.err.println("elasticSearch注入成功");
//        return new ElasticsearchClient(transport);
//    }


    //注入IOC容器
    @Bean
    public ElasticsearchClient elasticsearchClientApi() {


        String serverUrl = "http://192.168.1.19:9200";
        String apiKey = "Linson";

// 创建低级客户端
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                })
                .build();

// 使用 Jackson 映射器创建传输
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

// 并创建 API 客户端
        return new ElasticsearchClient(transport);

    }
}
