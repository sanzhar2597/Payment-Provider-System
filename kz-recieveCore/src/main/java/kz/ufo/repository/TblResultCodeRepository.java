package kz.ufo.repository;


import kz.ufo.entity.TblResultCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface TblResultCodeRepository extends JpaRepository<TblResultCode,Long> {

    @Query(value = "select *\n" +
            "  from tbl_result_code\n" +
            " where id in (select id_result\n" +
            "                from tbl_result_code2agents\n" +
            "               where code = :#{#code}\n" +
            "                 and agent_id = :#{#agentId})\n" , nativeQuery = true)
    TblResultCode findByCodeErrorAndAgentId(int code, int agentId);

}
