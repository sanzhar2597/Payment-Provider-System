package kz.ufo.repository;

import kz.ufo.entity.TblProviderDisplays;
import kz.ufo.entity.TblResultCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblProviderDisplaysRepository extends JpaRepository<TblProviderDisplays,Long> {
    @Query(value = "select *\n" +
            "  from tbl_provider_displays\n" +
            " where id in (select id_display\n" +
            "                from tbl_provider_displays2agents\n" +
            "               where code = :#{#code}\n" +
            "                 and agent_id = :#{#agentId})\n" +
            "   and id_provider in(select id_provider from tbl_providers2agents s where s.code= :#{#idProvider})\n", nativeQuery = true)
    TblProviderDisplays findByProviderAgentCode(String code, int agentId, String idProvider);

    List<TblProviderDisplays> findAllByIdProvider(long idProvider);
}
