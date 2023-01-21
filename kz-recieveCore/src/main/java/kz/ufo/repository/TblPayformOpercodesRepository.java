package kz.ufo.repository;

import kz.ufo.entity.TblPayformOpercodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblPayformOpercodesRepository extends JpaRepository<TblPayformOpercodes,String> {

}
