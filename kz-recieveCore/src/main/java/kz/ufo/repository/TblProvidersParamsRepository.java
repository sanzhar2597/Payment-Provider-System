package kz.ufo.repository;

import kz.ufo.entity.TblProvidersParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblProvidersParamsRepository extends JpaRepository<TblProvidersParams, Long> {

 List<TblProvidersParams> findAllByIdProvider(long idProvider);

}
