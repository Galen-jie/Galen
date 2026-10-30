package com.galen.seckill.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.galen.seckill.constant.GoodsStatus;
import com.galen.seckill.entity.Goods;
import com.galen.seckill.entity.GoodsCategory;
import com.galen.seckill.mapper.GoodsCategoryMapper;
import com.galen.seckill.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品搜索工具，供大模型通过 Function Calling 查询宠物用品并智能推荐。
 *
 * @author Galen
 * @since 2026-10-29
 */
@Slf4j
@AssistantTool
public class GoodsSearchTool {

    private final GoodsMapper goodsMapper;

    private final GoodsCategoryMapper goodsCategoryMapper;

    public GoodsSearchTool(GoodsMapper goodsMapper, GoodsCategoryMapper goodsCategoryMapper) {
        this.goodsMapper = goodsMapper;
        this.goodsCategoryMapper = goodsCategoryMapper;
    }

    @Tool(description = "查询宠物商城在售商品，支持关键词、分类、价格区间筛选，用于给用户智能推荐宠物用品")
    public List<GoodsBrief> searchGoods(
            @ToolParam(description = "关键词，模糊匹配商品名称/标题/详情，如'狗粮'、'猫砂'，可不传") String keyword,
            @ToolParam(description = "商品分类名称，如'狗粮'、'猫砂'、'玩具'，可不传") String categoryName,
            @ToolParam(description = "最低价格（元），可不传") BigDecimal minPrice,
            @ToolParam(description = "最高价格（元），可不传") BigDecimal maxPrice) {

        log.info("[Tool] searchGoods 被调用: keyword={}, categoryName={}, minPrice={}, maxPrice={}",
                keyword, categoryName, minPrice, maxPrice);

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, GoodsStatus.START);

        if (StringUtils.hasText(categoryName)) {
            GoodsCategory category = goodsCategoryMapper.selectOne(
                    new LambdaQueryWrapper<GoodsCategory>()
                            .like(GoodsCategory::getCategoryName, categoryName)
                            .last("limit 1"));
            if (category != null) {
                wrapper.eq(Goods::getCategoryId, category.getId());
            }
        }

        wrapper.ge(minPrice != null, Goods::getGoodsPrice, minPrice)
                .le(maxPrice != null, Goods::getGoodsPrice, maxPrice);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Goods::getGoodsName, keyword)
                    .or().like(Goods::getGoodsTitle, keyword)
                    .or().like(Goods::getGoodsDetail, keyword));
        }

        wrapper.orderByDesc(Goods::getCreateTime).last("limit 10");

        List<GoodsBrief> result = goodsMapper.selectList(wrapper).stream()
                .map(g -> GoodsBrief.from(g, categoryNameOf(g.getCategoryId())))
                .toList();
        log.info("[Tool] searchGoods 返回 {} 条", result.size());
        return result;
    }

    private String categoryNameOf(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        GoodsCategory category = goodsCategoryMapper.selectById(categoryId);
        return category == null ? null : category.getCategoryName();
    }

    /**
     * 精简后的商品信息，只保留推荐所需的核心字段。
     */
    public record GoodsBrief(Long id, String goodsName, String goodsTitle, BigDecimal goodsPrice,
                             String categoryName, Integer stock, String goodsDetail) {

        static GoodsBrief from(Goods g, String categoryName) {
            return new GoodsBrief(
                    g.getId(),
                    g.getGoodsName(),
                    g.getGoodsTitle(),
                    g.getGoodsPrice(),
                    categoryName,
                    g.getStock(),
                    truncate(g.getGoodsDetail(), 60));
        }

        private static String truncate(String s, int max) {
            if (s == null) {
                return null;
            }
            return s.length() <= max ? s : s.substring(0, max) + "...";
        }
    }
}
