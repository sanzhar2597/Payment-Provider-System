package kz.ufo.repository;

import kz.ufo.entity.TblProvidersParamExtra;
import kz.ufo.entity.TblProvidersParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblProvidersParamExtraRepository extends JpaRepository<TblProvidersParamExtra ,Long> {
    List<TblProvidersParamExtra> findAllByIdProvider(long idProvider);
}
