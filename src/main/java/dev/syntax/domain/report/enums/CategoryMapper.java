package dev.syntax.domain.report.enums;

import java.util.HashMap;
import java.util.Map;

public class CategoryMapper {
        private static final Map<Category, String> CATEGORY_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put(Category.SHOPPING, "쇼핑");
        CATEGORY_MAP.put(Category.EDU, "교육");
        CATEGORY_MAP.put(Category.TRANSPORT, "교통");
        CATEGORY_MAP.put(Category.TRANSFER, "이체");
        CATEGORY_MAP.put(Category.ENT, "여가");
        CATEGORY_MAP.put(Category.FOOD, "식비");
        CATEGORY_MAP.put(Category.ETC, "기타");
    }

    /**
     * 영문 카테고리를 한글 표시명으로 변환합니다.
     * 일치하지 않을 경우 "기타" 반환합니다.
     *
     * @param category 영문 카테고리 코드
     * @return 한글 카테고리명
     */
    public static String toKorean(Category category) {
        if (category == null) return "기타";
        return CATEGORY_MAP.getOrDefault(category, "기타");
    }
}
