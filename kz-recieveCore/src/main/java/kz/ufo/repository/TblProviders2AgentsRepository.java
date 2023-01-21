package kz.ufo.repository;

import kz.ufo.entity.TblProviders2Agents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblProviders2AgentsRepository extends JpaRepository<TblProviders2Agents, Long> {


    @Query(value = "select * from (select x.*, row_number() over(partition by id_provider order by active desc) as rownumber from TBL_PROVIDERS2AGENTS x )\n" +
            "where rownumber = 1", nativeQuery = true)
    List<TblProviders2Agents> findUniqueByIdOrderByPriority();


    List<TblProviders2Agents> findListByIdProviderAndActive(long idProvider, int active);

    TblProviders2Agents findByCode(String code);


    @Query(value = "select * from TBL_PROVIDERS2AGENTS t where agent_id=1 ", nativeQuery = true)
    List<TblProviders2Agents> findQiwiProvidersByIdAgent();

    @Query(value = "select * from TBL_PROVIDERS2AGENTS t where agent_id=2 ", nativeQuery = true)
    List<TblProviders2Agents> findPayformProvidersByIdAgent();

    TblProviders2Agents findByIdProviderAndActiveAndAgentId(long idprovider,int active,int agentId);

    List<TblProviders2Agents> findByIdProviderAndActive(long idProvider,int active);
}
