package kz.ufo.repository;

import kz.ufo.entity.TblQiwiResultCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TblQiwiResultCodesRepository extends JpaRepository<TblQiwiResultCode,Long> {
}
