package kz.ufo.repository;

import kz.ufo.entity.TblSubagents;
import lombok.SneakyThrows;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblSubagentsRepository extends JpaRepository<TblSubagents,Integer> {
    @Query(value = "select * from tbl_subagents where code = :#{#code}\n" , nativeQuery = true)
    TblSubagents findLoginPassbyCode(String code);


    @SneakyThrows
    @Query(value = "select to_number(dm.P_CALENDAR.f_ISWORKINGDAY@dm.hcb.kz(sysdate)) from dual\n" , nativeQuery = true)
    Integer checkWorkingDayToday();

    @SneakyThrows
    @Query(value = "select to_number(dm.P_CALENDAR.f_ISWORKINGDAY@dm.hcb.kz(sysdate+ :#{#days})) from dual\n" , nativeQuery = true)
    Integer checkWorkingDayTomorrow(int days);


    List<TblSubagents> findByIdAgent(int idAgent);

    TblSubagents findByCode(String code);

}
