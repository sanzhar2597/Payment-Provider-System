package kz.ufo.repository;

import kz.ufo.entity.TblProvidersParams;
import kz.ufo.entity.TblProvidersParamsExtraTypeValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblProvidersParamsExtraTypeValuesRepository  extends JpaRepository<TblProvidersParamsExtraTypeValues,Long> {
    List<TblProvidersParamsExtraTypeValues> findAllByIdProvider(long idProvider);
}
