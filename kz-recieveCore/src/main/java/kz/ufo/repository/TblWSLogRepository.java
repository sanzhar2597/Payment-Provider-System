package kz.ufo.repository;

import kz.ufo.entity.TblWSLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblWSLogRepository extends CrudRepository<TblWSLog, Long> {
}
