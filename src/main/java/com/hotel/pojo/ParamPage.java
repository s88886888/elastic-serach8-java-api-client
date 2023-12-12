package com.hotel.pojo;


import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class ParamPage {

    private String key;

    private Integer page;

    private Integer size;

    private String sortBy;
}
