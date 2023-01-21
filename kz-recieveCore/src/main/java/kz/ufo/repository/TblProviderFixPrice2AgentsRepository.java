package kz.ufo.repository;

import kz.ufo.entity.TblProviderFixPrice2Agents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblProviderFixPrice2AgentsRepository extends JpaRepository<TblProviderFixPrice2Agents, Integer> {
     TblProviderFixPrice2Agents findByIdProviderPartnerAndAgentId(String idproviderPartner,int agentId);
}
