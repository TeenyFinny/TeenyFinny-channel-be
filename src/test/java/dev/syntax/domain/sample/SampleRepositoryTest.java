package dev.syntax.domain.sample;

import dev.syntax.domain.sample.entity.SampleEntity;
import dev.syntax.domain.sample.repository.SampleRepository;
import dev.syntax.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



@DataJpaTest
// application-test.yml을 불러온다.
@ActiveProfiles("test")
// JpaConfig 설정 파일을 불러온다
@Import(JpaConfig.class)
// 테스트용 H2 임시 DB 사용
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class SampleRepositoryTest {

    @Autowired
    private SampleRepository sampleRepository;

    @PersistenceContext
    private EntityManager em;

    private SampleEntity newEntity(String price) {
        SampleEntity e = new SampleEntity();
        e.setPrice(price);
        return e;
    }

    @Test
    @DisplayName("저장 후 id로 조회하면 동일한 엔티티를 얻는다")
    void saveAndFindById() {
        // given
        SampleEntity saved = sampleRepository.save(newEntity("12345"));
        // when
        Optional<SampleEntity> found = sampleRepository.findById(saved.getId());
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualTo("12345");
    }

    @Test
    @DisplayName("findAll은 저장된 전체 엔티티를 반환한다")
    void findAll() {
        // given
        sampleRepository.save(newEntity("100"));
        sampleRepository.save(newEntity("200"));
        // when
        List<SampleEntity> all = sampleRepository.findAll();
        // then
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        assertThat(all).extracting(SampleEntity::getPrice).contains("100", "200");
    }

    @Test
    @DisplayName("price가 null이면 NOT NULL 제약으로 예외가 발생한다")
    void saveNullPriceShouldFail() {
        // given
        SampleEntity e = new SampleEntity(); // price 미설정(null)
        // when / then
        assertThatThrownBy(() -> {
            sampleRepository.saveAndFlush(e); // flush로 즉시 제약 검사
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Rollback // 가독성용(디폴트 true), updatable=false 동작 확인
    @DisplayName("updatable=false: 저장 후 price를 바꿔도 UPDATE에 포함되지 않아 DB값은 그대로다")
    void updatePriceIgnoredDueToUpdatableFalse() {
        // given: 최초 저장
        SampleEntity saved = sampleRepository.saveAndFlush(newEntity("1000"));

        // when: price 변경 시도 후 flush
        saved.setPrice("9999"); // @Column(updatable=false) 이므로 UPDATE SQL에 포함되지 않아야 함
        sampleRepository.saveAndFlush(saved);

        // 영속성 컨텍스트 초기화 후 다시 조회(1차 캐시 영향 제거)
        em.clear();
        SampleEntity reloaded = sampleRepository.findById(saved.getId()).orElseThrow();

        // then: DB의 price는 최초 값 유지
        assertThat(reloaded.getPrice()).isEqualTo("1000");
    }

    @Nested
    @DisplayName("삭제 동작")
    class DeleteTests {

        @Test
        @DisplayName("삭제 후 조회 시 존재하지 않는다")
        void delete() {
            // given
            SampleEntity e = sampleRepository.saveAndFlush(newEntity("77"));
            Long id = e.getId();

            // when
            sampleRepository.deleteById(id);
            sampleRepository.flush();

            // then
            assertThat(sampleRepository.findById(id)).isEmpty();
        }
    }
}
