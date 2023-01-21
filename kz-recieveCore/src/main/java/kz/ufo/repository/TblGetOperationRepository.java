package kz.ufo.repository;

import kz.ufo.entity.TblGetOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Repository
@Transactional
public interface TblGetOperationRepository extends JpaRepository<TblGetOperation,BigInteger> {

    @Query(value = "select SEQ_TBL_PAYOPER.NEXTVAL from dual", nativeQuery = true)
    BigInteger getSeqId();

    @Query(value = "select SEQ_TBL_PAYMENT_QIWI_BALANCE.NEXTVAL from dual", nativeQuery = true)
    Integer getBalanceSeqId();
}
