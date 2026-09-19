package com.galen.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.GoodsCategoryDTO;
import com.galen.seckill.entity.GoodsCategory;
import com.galen.seckill.exception.BusinessException;
import com.galen.seckill.mapper.GoodsCategoryMapper;
import com.galen.seckill.service.GoodsCategoryService;
import com.galen.seckill.vo.GoodsCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsCategoryServiceImpl implements GoodsCategoryService {

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public GoodsCategoryVO createCategory(GoodsCategoryDTO goodsCategoryDTO) {
        GoodsCategory existing = goodsCategoryMapper.selectByName(goodsCategoryDTO.getCategoryName());
        if (existing != null) {
            throw new BusinessException("分类名称已存在");
        }

        if (goodsCategoryDTO.getParentId() != null && goodsCategoryDTO.getParentId() > 0) {
            GoodsCategory parent = goodsCategoryMapper.selectById(goodsCategoryDTO.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
        }

        GoodsCategory goodsCategory = BeanUtil.copyProperties(goodsCategoryDTO, GoodsCategory.class);
        goodsCategory.setCreateTime(LocalDateTime.now());
        goodsCategory.setUpdateTime(LocalDateTime.now());
        goodsCategoryMapper.insert(goodsCategory);

        return BeanUtil.copyProperties(goodsCategory, GoodsCategoryVO.class);
    }

    @Override
    public GoodsCategoryVO updateCategory(Long id, GoodsCategoryDTO goodsCategoryDTO) {
        GoodsCategory existing = goodsCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        if (!existing.getCategoryName().equals(goodsCategoryDTO.getCategoryName())) {
            GoodsCategory nameCheck = goodsCategoryMapper.selectByName(goodsCategoryDTO.getCategoryName());
            if (nameCheck != null) {
                throw new BusinessException("分类名称已存在");
            }
        }

        if (goodsCategoryDTO.getParentId() != null && goodsCategoryDTO.getParentId() > 0) {
            if (goodsCategoryDTO.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设置为父分类");
            }
            GoodsCategory parent = goodsCategoryMapper.selectById(goodsCategoryDTO.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
        }

        GoodsCategory goodsCategory = BeanUtil.copyProperties(goodsCategoryDTO, GoodsCategory.class);
        goodsCategory.setId(id);
        goodsCategory.setUpdateTime(LocalDateTime.now());
        goodsCategoryMapper.updateById(goodsCategory);

        return BeanUtil.copyProperties(goodsCategory, GoodsCategoryVO.class);
    }

    @Override
    public void deleteCategory(Long id) {
        GoodsCategory existing = goodsCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsCategory::getParentId, id);
        Long childCount = goodsCategoryMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new BusinessException("存在子分类，无法删除");
        }

        goodsCategoryMapper.deleteById(id);
    }

    @Override
    public GoodsCategoryVO getCategoryById(Long id) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectById(id);
        if (goodsCategory == null) {
            throw new BusinessException("分类不存在");
        }
        return BeanUtil.copyProperties(goodsCategory, GoodsCategoryVO.class);
    }

    @Override
    public PageResult<GoodsCategoryVO> getCategoryList(int pageNum, int pageSize) {
        Page<GoodsCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(GoodsCategory::getSort)
               .orderByDesc(GoodsCategory::getCreateTime);

        Page<GoodsCategory> result = goodsCategoryMapper.selectPage(page, wrapper);

        List<GoodsCategoryVO> voList = result.getRecords().stream()
                .map(entity -> BeanUtil.copyProperties(entity, GoodsCategoryVO.class))
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public List<GoodsCategoryVO> getCategoryTree() {
        List<GoodsCategory> allCategories = goodsCategoryMapper.selectList(null);

        List<GoodsCategoryVO> voList = allCategories.stream()
                .map(entity -> BeanUtil.copyProperties(entity, GoodsCategoryVO.class))
                .collect(Collectors.toList());

        Map<Long, List<GoodsCategoryVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(GoodsCategoryVO::getParentId));

        voList.forEach(vo -> {
            List<GoodsCategoryVO> children = parentMap.get(vo.getId());
            vo.setChildren(children != null ? new ArrayList<>(children) : new ArrayList<GoodsCategoryVO>());
        });

        return voList.stream()
                .filter(vo -> vo.getParentId() == null || vo.getParentId() == 0)
                .collect(Collectors.toList());
    }
}