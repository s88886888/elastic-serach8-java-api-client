package com.spingcould.es8.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
@TableName("tb_hotel")
public class Hotel {


    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String longitude;
    private String latitude;
    private String pic;

    public Hotel(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Hotel() {
    }
}
