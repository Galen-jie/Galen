package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.SeckillOrderDTO;
import com.galen.seckill.entity.Goods;
import com.galen.seckill.entity.SeckillGoods;
import com.galen.seckill.entity.SeckillOrder;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.GoodsMapper;
import com.galen.seckill.mapper.SeckillGoodsMapper;
import com.galen.seckill.mapper.SeckillOrderMapper;
import com.galen.seckill.mq.message.SeckillMessage;
import com.galen.seckill.mq.producer.SeckillProducer;
import com.galen.seckill.service.SeckillOrderService;
import com.galen.seckill.util.StockRedisUtil;
import com.galen.seckill.util.UserHolder;
import com.galen.seckill.vo.SeckillOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SeckillProducer seckillProducer;

    @Autowired
    private StockRedisUtil stockRedisUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public String createSeckillOrder(SeckillOrderDTO seckillOrderDTO) {
        if (UserHolder.getUser() == null) {
            throw new BusinessException("用户未登录");
        }
        Long userId = UserHolder.getUser().getId();
        String seckillId = seckillOrderDTO.getSeckillId();

        SeckillGoods seckillGoods = seckillGoodsMapper.selectBySeckillId(seckillId);
        if (seckillGoods == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        // 时间窗口校验（以时间为准，不依赖定时任务刷新的 status 字段）
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillGoods.getStartTime())) {
            throw new BusinessException("秒杀活动未开始");
        }
        if (now.isAfter(seckillGoods.getEndTime())) {
            throw new BusinessException("秒杀活动已结束");
        }

        // 查商品信息（提前查，供消息体用；查不到就不扣库存）
        Goods goods = goodsMapper.selectById(seckillGoods.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        // 用户级分布式锁：防止同一用户并发重复下单
        String lockKey = "galen:lock:user:" + userId + ":" + seckillId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // 最多等 3 秒抢锁，锁 30 秒自动释放
            locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("操作过于频繁，请稍后再试");
            }

            // 判重：同一用户同一秒杀只能买一次
            SeckillOrder existing = seckillOrderMapper.selectByUserIdAndSeckillId(
                    userId, seckillId, seckillOrderDTO.getGoodsId());
            if (existing != null) {
                throw new BusinessException("您已参与过此秒杀活动，不能重复购买");
            }

            // 第一道防线：Redis Lua 原子扣库存
            boolean decreased = stockRedisUtil.decreaseStock(seckillId, 1);
            if (!decreased) {
                throw new BusinessException("秒杀商品已售罄");
            }

            // 生成订单号 + 构建消息（订单此时还没建，交给消费者异步落库）
            String orderNo = "SK" + IdUtil.getSnowflakeNextIdStr();
            SeckillMessage message = new SeckillMessage();
            message.setUserId(userId);
            message.setGoodsId(seckillGoods.getGoodsId());
            message.setSeckillId(seckillId);
            message.setSeckillGoodsId(seckillGoods.getId());
            message.setOrderNo(orderNo);
            message.setGoodsName(goods.getGoodsName());
            message.setGoodsImg(goods.getGoodsImg());
            message.setOrderPrice(seckillGoods.getSeckillPrice());
            message.setReceiverName(seckillOrderDTO.getReceiverName());
            message.setReceiverPhone(seckillOrderDTO.getReceiverPhone());
            message.setReceiverAddress(seckillOrderDTO.getReceiverAddress());

            // 发 MQ 异步下单
            seckillProducer.send(message);
            log.info("秒杀请求已提交，orderNo: {}, userId: {}, seckillId: {}", orderNo, userId, seckillId);

            return orderNo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
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
    public SeckillOrderVO getSeckillResult(String orderNo) {
        // 轮询用：订单还没建出来时返回 null（前端据此判断"处理中"），而不是抛异常
        SeckillOrder order = seckillOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return null;
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