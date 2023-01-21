package kz.ufo.repository;

import kz.ufo.entity.TblQiwiProviderParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblQiwiProviderParamRepository extends JpaRepository<TblQiwiProviderParam,Long> {
}
