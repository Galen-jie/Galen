package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.GoodsDTO;
import com.galen.seckill.entity.Goods;
import com.galen.seckill.entity.GoodsCategory;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.GoodsCategoryMapper;
import com.galen.seckill.mapper.GoodsMapper;
import com.galen.seckill.service.GoodsService;
import com.galen.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public GoodsVO createGoods(GoodsDTO goodsDTO) {
        Goods existing = goodsMapper.selectByName(goodsDTO.getGoodsName());
        if (existing != null) {
            throw new BusinessException("商品名称已存在");
        }

        GoodsCategory category = goodsCategoryMapper.selectById(goodsDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException("商品分类不存在");
        }

        Goods goods = BeanUtil.copyProperties(goodsDTO, Goods.class);
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.insert(goods);

        return convertToVO(goods);
    }

    @Override
    public GoodsVO updateGoods(Long id, GoodsDTO goodsDTO) {
        Goods existing = goodsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }

        if (!existing.getGoodsName().equals(goodsDTO.getGoodsName())) {
            Goods nameCheck = goodsMapper.selectByName(goodsDTO.getGoodsName());
            if (nameCheck != null) {
                throw new BusinessException("商品名称已存在");
            }
        }

        GoodsCategory category = goodsCategoryMapper.selectById(goodsDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException("商品分类不存在");
        }

        Goods goods = BeanUtil.copyProperties(goodsDTO, Goods.class);
        goods.setId(id);
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);

        Goods updated = goodsMapper.selectById(id);
        return convertToVO(updated);
    }

    @Override
    public void deleteGoods(Long id) {
        Goods existing = goodsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        goodsMapper.deleteById(id);
    }

    @Override
    public GoodsVO getGoodsById(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return convertToVO(goods);
    }

    @Override
    public PageResult<GoodsVO> getGoodsList(int pageNum, int pageSize, Long categoryId, Integer status) {
        Page<Goods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        wrapper.orderByDesc(Goods::getCreateTime);

        Page<Goods> result = goodsMapper.selectPage(page, wrapper);

        List<GoodsVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public boolean decreaseStock(Long id, Integer count) {
        if (count == null || count <= 0) {
            return false;
        }
        return goodsMapper.decreaseStock(id, count) > 0;
    }

    @Override
    public boolean increaseStock(Long id, Integer count) {
        if (count == null || count <= 0) {
            return false;
        }
        return goodsMapper.increaseStock(id, count) > 0;
    }

    private GoodsVO convertToVO(Goods goods) {
        GoodsVO vo = BeanUtil.copyProperties(goods, GoodsVO.class);
        if (goods.getCategoryId() != null) {
            GoodsCategory category = goodsCategoryMapper.selectById(goods.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        return vo;
    }
}