package kz.ufo.repository;

import kz.ufo.entity.TblProviders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TblProvidersRepository extends JpaRepository<TblProviders, Long> {
    TblProviders findById(long id);
}
