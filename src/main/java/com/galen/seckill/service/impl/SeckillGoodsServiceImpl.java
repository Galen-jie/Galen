package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.SeckillGoodsDTO;
import com.galen.seckill.entity.Goods;
import com.galen.seckill.entity.SeckillGoods;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.GoodsMapper;
import com.galen.seckill.mapper.SeckillGoodsMapper;
import com.galen.seckill.service.SeckillGoodsService;
import com.galen.seckill.util.RedisUtil;
import com.galen.seckill.vo.SeckillGoodsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public SeckillGoodsVO createSeckillGoods(SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoods existing = seckillGoodsMapper.selectBySeckillId(seckillGoodsDTO.getSeckillId());
        if (existing != null) {
            throw new BusinessException("秒杀ID已存在");
        }

        if (seckillGoodsDTO.getStartTime().isAfter(seckillGoodsDTO.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        if (seckillGoodsDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("开始时间不能早于当前时间");
        }

        Goods goods = goodsMapper.selectById(seckillGoodsDTO.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        SeckillGoods seckillGoods = BeanUtil.copyProperties(seckillGoodsDTO, SeckillGoods.class);
        seckillGoods.setCreateTime(LocalDateTime.now());
        seckillGoods.setUpdateTime(LocalDateTime.now());
        seckillGoods.setVersion(0);
        seckillGoodsMapper.insert(seckillGoods);

        // 预热到Redis
        redisUtil.preheatSeckillGoods(seckillGoods);
        log.info("创建秒杀商品成功，已预热到Redis。seckillId: {}", seckillGoods.getSeckillId());

        return convertToVO(seckillGoods);
    }

    @Override
    public SeckillGoodsVO updateSeckillGoods(Long id, SeckillGoodsDTO seckillGoodsDTO) {
        SeckillGoods existing = seckillGoodsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        if (!existing.getSeckillId().equals(seckillGoodsDTO.getSeckillId())) {
            SeckillGoods seckillIdCheck = seckillGoodsMapper.selectBySeckillId(seckillGoodsDTO.getSeckillId());
            if (seckillIdCheck != null) {
                throw new BusinessException("秒杀ID已存在");
            }
        }

        if (seckillGoodsDTO.getStartTime().isAfter(seckillGoodsDTO.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        Goods goods = goodsMapper.selectById(seckillGoodsDTO.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        SeckillGoods seckillGoods = BeanUtil.copyProperties(seckillGoodsDTO, SeckillGoods.class);
        seckillGoods.setId(id);
        seckillGoods.setUpdateTime(LocalDateTime.now());
        seckillGoods.setVersion(existing.getVersion());
        seckillGoodsMapper.updateById(seckillGoods);

        // 先更新DB，再异步延迟双删缓存
        redisUtil.deleteSeckillGoodsCacheAsync(existing.getSeckillId());
        log.info("更新秒杀商品成功，已触发缓存删除。seckillId: {}", existing.getSeckillId());

        SeckillGoods updated = seckillGoodsMapper.selectById(id);
        return convertToVO(updated);
    }

    @Override
    public void deleteSeckillGoods(Long id) {
        SeckillGoods existing = seckillGoodsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("秒杀商品不存在");
        }
        seckillGoodsMapper.deleteById(id);

        // 先删除DB，再异步延迟双删缓存
        redisUtil.deleteSeckillGoodsCacheAsync(existing.getSeckillId());
        log.info("删除秒杀商品成功，已触发缓存删除。seckillId: {}", existing.getSeckillId());
    }

    @Override
    public SeckillGoodsVO getSeckillGoodsById(Long id) {
        // 先从Redis查询
        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(id);
        if (seckillGoods == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // 尝试从Redis获取
        SeckillGoods cached = redisUtil.getSeckillGoodsFromRedis(seckillGoods.getSeckillId());
        if (cached != null) {
            log.info("从Redis获取秒杀商品信息。seckillId: {}", cached.getSeckillId());
            return convertToVO(cached);
        }

        // Redis没有，从数据库读取
        log.info("Redis未命中，从数据库读取秒杀商品信息。seckillId: {}", seckillGoods.getSeckillId());
        return convertToVO(seckillGoods);
    }

    @Override
    public SeckillGoodsVO getSeckillGoodsBySeckillId(String seckillId) {
        // 优先从Redis查询
        SeckillGoods cached = redisUtil.getSeckillGoodsFromRedis(seckillId);
        if (cached != null) {
            log.info("从Redis获取秒杀商品信息。seckillId: {}", seckillId);
            return convertToVO(cached);
        }

        // Redis没有，从数据库查询
        log.info("Redis未命中，从数据库查询秒杀商品信息。seckillId: {}", seckillId);
        SeckillGoods seckillGoods = seckillGoodsMapper.selectBySeckillId(seckillId);
        if (seckillGoods == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        return convertToVO(seckillGoods);
    }

    @Override
    public PageResult<SeckillGoodsVO> getSeckillGoodsList(int pageNum, int pageSize, Integer status) {
        Page<SeckillGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(SeckillGoods::getStatus, status);
        }
        wrapper.orderByDesc(SeckillGoods::getCreateTime);

        Page<SeckillGoods> result = seckillGoodsMapper.selectPage(page, wrapper);

        List<SeckillGoodsVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public List<SeckillGoodsVO> getActiveSeckillGoods() {
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillGoods::getStatus, 1);
        wrapper.orderByAsc(SeckillGoods::getStartTime);

        List<SeckillGoods> list = seckillGoodsMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public boolean decreaseStock(Long id, Integer count) {
        if (count == null || count <= 0) {
            return false;
        }

        SeckillGoods seckillGoods = seckillGoodsMapper.selectById(id);
        if (seckillGoods == null) {
            return false;
        }

        int result = seckillGoodsMapper.decreaseStockWithVersion(id, count, seckillGoods.getVersion());
        return result > 0;
    }

    private SeckillGoodsVO convertToVO(SeckillGoods seckillGoods) {
        SeckillGoodsVO vo = BeanUtil.copyProperties(seckillGoods, SeckillGoodsVO.class);

        Goods goods = goodsMapper.selectById(seckillGoods.getGoodsId());
        if (goods != null) {
            vo.setGoodsName(goods.getGoodsName());
            vo.setGoodsTitle(goods.getGoodsTitle());
            vo.setGoodsImg(goods.getGoodsImg());
            vo.setGoodsPrice(goods.getGoodsPrice());
        }

        vo.setOriginalStockCount(seckillGoods.getStockCount());

        vo.setStatusText(getStatusText(seckillGoods.getStatus()));

        return vo;
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "未开始";
            case 1:
                return "进行中";
            case 2:
                return "已结束";
            default:
                return "未知";
        }
    }
}