package kz.ufo.repository;

import kz.ufo.entity.TblSpr;
import kz.ufo.entity.TblSubagents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblSprRepository extends JpaRepository<TblSpr,Integer> {
        TblSpr findByCode(String code);
}
