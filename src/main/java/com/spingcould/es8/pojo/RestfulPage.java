package com.spingcould.es8.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
public class RestfulPage {

    private Long total;

    private List<HotelDoc> hotels;
}
