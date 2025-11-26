package dev.syntax.domain.report.enums;

/**
 * 소비 카테고리.
 */
public enum Category {
    SHOPPING("쇼핑"),
    EDU("교육"),
    TRANSPORT("교통"),
    TRANSFER("이체"),
    ENT("여가/문화"),
    FOOD("식비"),
    ETC("기타");

    private final String koreanName;

    Category(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
