package com.galen.seckill.service;


import com.galen.seckill.common.PageResult;
import com.galen.seckill.dto.GoodsCategoryDTO;
import com.galen.seckill.vo.GoodsCategoryVO;

import java.util.List;

public interface GoodsCategoryService {
    GoodsCategoryVO createCategory(GoodsCategoryDTO goodsCategoryDTO);

    GoodsCategoryVO updateCategory(Long id,GoodsCategoryDTO goodsCategoryDTO);

    void deleteCategory(Long id);

    GoodsCategoryVO getCategoryById(Long id);

    PageResult<GoodsCategoryVO> getCategoryList(int pageNum, int pageSize);

    List<GoodsCategoryVO> getCategoryTree();
}
