package com.secta.hcbatch.repository;

import com.secta.hcbatch.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsRepository extends JpaRepository<Goods, Long> {
    Goods findByGoodsName(String goodsCode);
}
