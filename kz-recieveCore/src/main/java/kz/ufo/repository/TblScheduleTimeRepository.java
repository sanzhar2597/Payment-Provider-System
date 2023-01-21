package kz.ufo.repository;

import kz.ufo.entity.TblScheduleTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblScheduleTimeRepository extends JpaRepository<TblScheduleTime,Long> {

    TblScheduleTime findByName(String name);

}
