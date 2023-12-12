package com.hotel.pojo;

import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
public class RestfulPage {

    private Long total;

    private List<Hotel> hotels;
}
