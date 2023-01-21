package kz.ufo.repository;

import kz.ufo.entity.TblQiwiOperCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TblQiwiOperCodesRepository extends JpaRepository<TblQiwiOperCodes, Long> {
     TblQiwiOperCodes findByName(String name);




}
