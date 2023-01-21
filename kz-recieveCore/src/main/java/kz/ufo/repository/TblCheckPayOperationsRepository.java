package kz.ufo.repository;

import kz.ufo.entity.TblCheckPayOperations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;
import java.util.List;

@Repository
@Transactional
public interface TblCheckPayOperationsRepository extends JpaRepository<TblCheckPayOperations, BigInteger> {
    TblCheckPayOperations findByTransactionIdAndMethodAndStatus(BigInteger transactionId,String method,int status);

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where status = 5\n" +
            "   and id_agent = 1\n" +
            "   and method in ('confirmQiwiPayment',\n" +
            "                  'addQiwiOfflinePayment',\n" +
            "                  'confirmQiwiExtraPayment')\n" +
            "   and transaction_date >= trunc(sysdate) - 3\n" +
            "   and systemName = 'IB'\n" +
            "   and id_provider in (select a.id_provider\n" +
            "                         from tbl_providers2agents a\n" +
            "                        where a.agent_id = 1\n" +
            "                          and a.complex != 1)", nativeQuery = true)
    List<TblCheckPayOperations> findbyStatusMethodDateIB();

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where status = 5\n" +
            "   and id_agent = 1\n" +
            "   and method in ('confirmQiwiPayment',\n" +
            "                  'addQiwiOfflinePayment',\n" +
            "                  'confirmQiwiExtraPayment')\n" +
            "   and transaction_date >= trunc(sysdate) - 3\n" +
            "   and systemName = 'TNT'\n" +
            "   and id_provider in (select a.id_provider\n" +
            "                         from tbl_providers2agents a\n" +
            "                        where a.agent_id = 1\n" +
            "                          and a.complex != 1)", nativeQuery = true)
    List<TblCheckPayOperations> findbyStatusMethodDateTNT();

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where status = 5\n" +
            "   and id_agent = 1\n" +
            "   and method in ('confirmQiwiPayment',\n" +
            "                  'addQiwiOfflinePayment',\n" +
            "                  'confirmQiwiExtraPayment')\n" +
            "   and transaction_date >= trunc(sysdate) - 3\n" +
            "   and systemName = 'IBCNP'\n" +
            "   and id_provider in (select a.id_provider\n" +
            "                         from tbl_providers2agents a\n" +
            "                        where a.agent_id = 1\n" +
            "                          and a.complex != 1)", nativeQuery = true)
    List<TblCheckPayOperations> findbyStatusMethodDateIBCNP();
    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where method !='check'\n" +
            "   and transaction_date >= trunc(sysdate)-1   and id_transaction= :#{#id} \n" , nativeQuery = true)
    TblCheckPayOperations findForGetStatusPay(BigInteger id);

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where method !='check'\n" +
            "   and transaction_date >= trunc(sysdate) - 1\n" +
            "   and sended = 0\n" +
            "   and systemname in ('IB', 'IBCNP')\n", nativeQuery = true)
    List<TblCheckPayOperations> findByMethodDateSended();

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where method !='check'\n" +
            "   and transaction_date >= trunc(sysdate) - 1\n" +
            "   and sended = 0\n" +
            "   and systemname = 'TNT'\n", nativeQuery = true)
    List<TblCheckPayOperations> findByMethodDateSendedTNT();

    @Query(value = "select *\n" +
            "            from tbl_check_pay_operations o\n" +
            "           where o.method in ('authQiwiPayment',\n" +
            "                              'authQiwiExtrasPayment',\n" +
            "                              'confirmQiwiPayment',\n" +
            "                              'confirmQiwiExtraPayment',\n" +
            "                              'addQiwiOfflinePayment',\n" +
            "                              'payService',\n" +
            "                              'payExtraService')\n" +
            "             and o.transaction_date >= trunc(sysdate) - 1\n" +
            "             and o.status = 2\n" +
            "             and o.id_count < (select count(*)*:#{#rePayCoef} \n" +
            "                     from tbl_providers2agents p\n" +
            "                    where p.id_provider = o.id_provider\n" +
            "                      and p.active = 1)\n" , nativeQuery = true)
    List<TblCheckPayOperations> findErrorPays(int rePayCoef);


    @Query(value = "select * from tbl_check_pay_operations where transaction_date>=trunc(sysdate)-1 and method='check' and status=1", nativeQuery = true)
    List<TblCheckPayOperations> findCheckTransactIdForPay();


    


    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations s\n" +
            " where s.transaction_date >= trunc(sysdate) - 1\n" +
            "   and s.status = 5\n" +
            "   and s.id_agent = 2\n" +
            "   and s.method != 'check'\n" +
            "   and s.id_provider in (select a.id_provider\n" +
            "                           from tbl_providers2agents a\n" +
            "                          where a.agent_id = 2\n" +
            "                            and a.complex != 1)", nativeQuery = true)
    List<TblCheckPayOperations> findInProcessingTransactions();

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations s\n" +
            " where s.transaction_date >= trunc(sysdate) - 1\n" +
            "   and s.status = 5\n" +
            "   and s.id_agent = 2\n" +
            "   and s.method != 'check'\n" +
            "   and s.id_provider in (select a.id_provider\n" +
            "                           from tbl_providers2agents a\n" +
            "                          where a.agent_id = 2\n" +
            "                            and a.complex = 1)", nativeQuery = true)
    List<TblCheckPayOperations> findInProcessingTransactionsComplex();

    @Query(value = "select *\n" +
            "             from TBL_CHECK_PAY_OPERATIONS t\n" +
            "            where (trunc(t.transaction_date) between trunc(sysdate-:#{#countDays}) and trunc(sysdate-1) )\n" +
            "              and status = 6\n" +
            "              and payed_agr = 0\n" +
            "              and id_agent = 2\n" +
            "              and systemName = 'IB' and method!='check'", nativeQuery = true)
    List<TblCheckPayOperations> sumTransactionsPayedwithAgentPayformIB(int countDays);

    @Query(value = "select *\n" +
            "             from TBL_CHECK_PAY_OPERATIONS t\n" +
            "            where (trunc(t.transaction_date) between trunc(sysdate-:#{#countDays}) and trunc(sysdate-1) )\n" +
            "              and status = 6\n" +
            "              and payed_agr = 0\n" +
            "              and id_agent = 2\n" +
            "              and systemName = 'TNT' and method!='check'", nativeQuery = true)
    List<TblCheckPayOperations> sumTransactionsPayedwithAgentPayformTNT(int countDays);

    @Query(value = "select *\n" +
            "             from TBL_CHECK_PAY_OPERATIONS t\n" +
            "            where (trunc(t.transaction_date) between trunc(sysdate-:#{#countDays}) and trunc(sysdate-1) )\n" +
            "              and status = 6\n" +
            "              and payed_agr = 0\n" +
            "              and id_agent = 2\n" +
            "              and systemName = 'IBCNP' and method!='check'", nativeQuery = true)
    List<TblCheckPayOperations> sumTransactionsPayedwithAgentPayformIBCNP(int countDays);


    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where status = 5\n" +
            "   and id_agent = 1\n" +
            "   and method in ('confirmQiwiPayment',\n" +
            "                  'addQiwiOfflinePayment',\n" +
            "                  'confirmQiwiExtraPayment')\n" +
            "   and transaction_date >= trunc(sysdate) - 3\n" +
            "   and systemName = 'IB'\n" +
            "   and id_provider in (select a.id_provider\n" +
            "                         from tbl_providers2agents a\n" +
            "                        where a.agent_id = 1\n" +
            "                          and a.complex = 1)", nativeQuery = true)
    List<TblCheckPayOperations> findbyStatusMethodDateIBComplex();

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where status = 5\n" +
            "   and id_agent = 1\n" +
            "   and method in ('confirmQiwiPayment',\n" +
            "                  'addQiwiOfflinePayment',\n" +
            "                  'confirmQiwiExtraPayment')\n" +
            "   and transaction_date >= trunc(sysdate) - 3\n" +
            "   and systemName = 'TNT'\n" +
            "   and id_provider in (select a.id_provider\n" +
            "                         from tbl_providers2agents a\n" +
            "                        where a.agent_id = 1\n" +
            "                          and a.complex = 1)", nativeQuery = true)
    List<TblCheckPayOperations> findbyStatusMethodDateTNTComplex();

    @Query(value = "select *\n" +
            "  from tbl_check_pay_operations\n" +
            " where status = 5\n" +
            "   and id_agent = 1\n" +
            "   and method in ('confirmQiwiPayment',\n" +
            "                  'addQiwiOfflinePayment',\n" +
            "                  'confirmQiwiExtraPayment')\n" +
            "   and transaction_date >= trunc(sysdate) - 3\n" +
            "   and systemName = 'IBCNP'\n" +
            "   and id_provider in (select a.id_provider\n" +
            "                         from tbl_providers2agents a\n" +
            "                        where a.agent_id = 1\n" +
            "                          and a.complex = 1)", nativeQuery = true)
    List<TblCheckPayOperations> findbyStatusMethodDateIBCNPComplex();


}
