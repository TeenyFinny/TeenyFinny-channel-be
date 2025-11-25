package dev.syntax.domain.profile.dto;

/**
 * 사용자 프로필 정보 DTO
 * <p>
 * 사용자의 기본 정보(이름, 이메일, 전화번호)를 담는 객체입니다.
 * </p>
 */
public record UserProfile(

	String name,

	String email,

	String phoneNumber
) {
}
