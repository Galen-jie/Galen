package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.SeckillOrderDTO;
import com.galen.seckill.entity.SeckillGoods;
import com.galen.seckill.entity.SeckillOrder;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.SeckillGoodsMapper;
import com.galen.seckill.mapper.SeckillOrderMapper;
import com.galen.seckill.service.SeckillOrderService;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.SeckillOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public SeckillOrderVO createSeckillOrder(SeckillOrderDTO seckillOrderDTO) {
        if (UserHolder.getUser() == null) {
            throw new BusinessException("用户未登录");
        }
        Long userId = UserHolder.getUser().getId();

        SeckillGoods seckillGoods = seckillGoodsMapper.selectBySeckillId(seckillOrderDTO.getSeckillId());
        if (seckillGoods == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        if (seckillGoods.getStatus() != 1) {
            throw new BusinessException("秒杀活动未开始或已结束");
        }

        if (seckillGoods.getStockCount() <= 0) {
            throw new BusinessException("秒杀商品已售罄");
        }

        SeckillOrder existing = seckillOrderMapper.selectByUserIdAndSeckillId(
                userId, seckillOrderDTO.getSeckillId(), seckillOrderDTO.getGoodsId());
        if (existing != null) {
            throw new BusinessException("您已参与过此秒杀活动，不能重复购买");
        }

        SeckillOrder order = new SeckillOrder();
        order.setOrderNo("SK" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setSeckillId(seckillOrderDTO.getSeckillId());
        order.setGoodsId(seckillOrderDTO.getGoodsId());
        order.setSeckillGoodsId(seckillGoods.getId());
        order.setGoodsName(seckillGoods.getSeckillId());
        order.setOrderPrice(seckillGoods.getSeckillPrice());
        order.setStatus(0);
        order.setReceiverName(seckillOrderDTO.getReceiverName());
        order.setReceiverPhone(seckillOrderDTO.getReceiverPhone());
        order.setReceiverAddress(seckillOrderDTO.getReceiverAddress());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        seckillOrderMapper.insert(order);

        return convertToVO(order);
    }

    @Override
    public SeckillOrderVO getOrderById(Long id) {
        SeckillOrder order = seckillOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToVO(order);
    }

    @Override
    public SeckillOrderVO getOrderByOrderNo(String orderNo) {
        SeckillOrder order = seckillOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToVO(order);
    }

    @Override
    public PageResult<SeckillOrderVO> getMyOrders(Long userId, int pageNum, int pageSize) {
        Page<SeckillOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SeckillOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrder::getUserId, userId);
        wrapper.orderByDesc(SeckillOrder::getCreateTime);

        Page<SeckillOrder> result = seckillOrderMapper.selectPage(page, wrapper);

        List<SeckillOrderVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public SeckillOrderVO payOrder(Long orderId, Integer payType) {
        SeckillOrder order = seckillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        if (payType == null || (payType != 1 && payType != 2)) {
            throw new BusinessException("支付方式不正确");
        }

        order.setStatus(1);
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        seckillOrderMapper.updateById(order);

        return convertToVO(order);
    }

    @Override
    public SeckillOrderVO cancelOrder(Long orderId) {
        SeckillOrder order = seckillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，无法取消");
        }

        order.setStatus(4);
        order.setUpdateTime(LocalDateTime.now());
        seckillOrderMapper.updateById(order);

        return convertToVO(order);
    }

    private SeckillOrderVO convertToVO(SeckillOrder order) {
        SeckillOrderVO vo = BeanUtil.copyProperties(order, SeckillOrderVO.class);
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setPayTypeText(getPayTypeText(order.getPayType()));
        return vo;
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "已支付";
            case 2:
                return "已发货";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知";
        }
    }

    private String getPayTypeText(Integer payType) {
        if (payType == null) {
            return "";
        }
        switch (payType) {
            case 1:
                return "支付宝";
            case 2:
                return "微信";
            default:
                return "";
        }
    }
}