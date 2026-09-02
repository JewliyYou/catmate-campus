package com.catmate.cat;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CatRepository extends JpaRepository<Cat,Long> { boolean existsByCode(String code); Optional<Cat> findByCode(String code); long deleteByCodeIn(Collection<String> codes); }
