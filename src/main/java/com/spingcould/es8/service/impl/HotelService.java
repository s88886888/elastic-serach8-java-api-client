package com.spingcould.es8.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spingcould.es8.mapper.HotelMapper;
import com.spingcould.es8.pojo.Hotel;
import com.spingcould.es8.service.IHotelService;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
