package com.zjtelcom.cpct.service.channel;

import com.zjtelcom.cpct.domain.channel.PpmProduct;

import java.util.List;
import java.util.Map;

public interface ProductService {

    List<PpmProduct>  getProductList(Long userId,String productName);



}