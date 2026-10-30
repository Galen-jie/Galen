package com.galen.seckill.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.galen.seckill.constant.PetStatus;
import com.galen.seckill.constant.PetType;
import com.galen.seckill.entity.PetRescue;
import com.galen.seckill.mapper.PetRescueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 宠物搜索工具，供大模型通过 Function Calling 查询待领养宠物并智能推荐。
 *
 * @author Galen
 * @since 2026-10-29
 */
@Slf4j
@AssistantTool
public class PetSearchTool {

    private final PetRescueMapper petRescueMapper;

    public PetSearchTool(PetRescueMapper petRescueMapper) {
        this.petRescueMapper = petRescueMapper;
    }

    @Tool(description = "查询平台待领养的宠物信息，支持按类型、品种、毛色、性别、年龄筛选，用于给用户智能推荐宠物")
    public List<PetBrief> searchPets(
            @ToolParam(description = "宠物类型：1-狗，2-猫，3-其他，可不传") Integer petType,
            @ToolParam(description = "品种，如'布偶'、'金毛'，可不传") String breed,
            @ToolParam(description = "毛色，如'白色'、'橘色'，可不传") String color,
            @ToolParam(description = "性别：1-公，2-母，可不传") Integer gender,
            @ToolParam(description = "年龄，如'幼年'、'成年'，可不传") String age,
            @ToolParam(description = "关键词，模糊匹配宠物名称或描述，可不传") String keyword) {

        log.info("[Tool] searchPets 被调用: petType={}, breed={}, color={}, gender={}, age={}, keyword={}",
                petType, breed, color, gender, age, keyword);

        LambdaQueryWrapper<PetRescue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(petType != null, PetRescue::getPetType, petType)
                .eq(gender != null, PetRescue::getPetGender, gender)
                .eq(StringUtils.hasText(age), PetRescue::getPetAge, age)
                .like(StringUtils.hasText(breed), PetRescue::getPetBreed, breed)
                .like(StringUtils.hasText(color), PetRescue::getPetColor, color)
                .eq(PetRescue::getStatus, PetStatus.WAITING);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PetRescue::getPetName, keyword)
                    .or().like(PetRescue::getDescription, keyword));
        }

        wrapper.orderByDesc(PetRescue::getCreateTime).last("limit 10");

        List<PetBrief> result = petRescueMapper.selectList(wrapper).stream()
                .map(PetBrief::from)
                .toList();
        log.info("[Tool] searchPets 返回 {} 条", result.size());
        return result;
    }

    /**
     * 精简后的宠物信息，只保留推荐所需的核心字段，避免把图片/视频等大字段返回给模型。
     */
    public record PetBrief(Long id, String petName, String petTypeName, String petBreed,
                           String petGenderName, String petAge, String petColor,
                           String healthStatus, String rescueLocation, String description) {

        static PetBrief from(PetRescue p) {
            return new PetBrief(
                    p.getId(),
                    p.getPetName(),
                    PetType.getName(p.getPetType()),
                    p.getPetBreed(),
                    genderName(p.getPetGender()),
                    p.getPetAge(),
                    p.getPetColor(),
                    p.getHealthStatus(),
                    p.getRescueLocation(),
                    truncate(p.getDescription(), 60));
        }

        private static String genderName(Integer gender) {
            if (gender == null) {
                return "未知";
            }
            return switch (gender) {
                case 1 -> "公";
                case 2 -> "母";
                default -> "未知";
            };
        }

        private static String truncate(String s, int max) {
            if (s == null) {
                return null;
            }
            return s.length() <= max ? s : s.substring(0, max) + "...";
        }
    }
}
