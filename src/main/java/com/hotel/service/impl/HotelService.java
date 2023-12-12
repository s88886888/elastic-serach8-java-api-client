package com.hotel.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.mapper.HotelMapper;
import com.hotel.pojo.Hotel;
import com.hotel.service.IHotelService;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
