package kz.ufo.repository;

import kz.ufo.entity.TblResultCode2Agents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblResultCode2AgentsRepository extends JpaRepository<TblResultCode2Agents,Integer> {
    @Query(value = "select *\n" +
            "                from tbl_result_code2agents\n" +
            "               where id_result = :#{#idResult}\n" +
            "                 and agent_id = :#{#agentId}\n" , nativeQuery = true)
    TblResultCode2Agents findFatal(int idResult, int agentId);
}

