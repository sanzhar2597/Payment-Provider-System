package kz.ufo.repository;

import kz.ufo.entity.TblPaymentQiwiBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public interface TblPaymentQiwiBalanceRepository extends JpaRepository<TblPaymentQiwiBalance,Integer> {

    @Query(value = "select trunc(sysdate) - trunc(datetime) as dateCount\n" +
            "  from (select x.*,\n" +
            "               row_number() over(partition by systemname order by datetime desc) as rownumber\n" +
            "          from tbl_payment_qiwi_balance x\n" +
            "         where x.id_agent = 2\n" +
            "           and x.systemname = 'IB')\n" +
            " where rownumber = 1", nativeQuery = true)
    Integer oldPaytoPayformCountIB();

    @Query(value = "select\n" +
            "       (case\n" +
            "         when trunc(sysdate) - trunc(datetime) > 1 then\n" +
            "          substr(datetime, 0, 2) || '-' || substr(trunc(sysdate), 0, 2)  ||\n" +
            "          substr(to_char(trunc(sysdate), 'dd.mm.yyyy'), 3, 8)\n" +
            "         else\n" +
            "          to_char(trunc(datetime), 'dd.mm.yyyy' )\n" +
            "       end) as dates\n" +
            "  from (select x.*,\n" +
            "               row_number() over(partition by systemname order by datetime desc) as rownumber\n" +
            "          from tbl_payment_qiwi_balance x\n" +
            "         where x.id_agent = 2\n" +
            "           and x.systemname = 'IB')\n" +
            " where rownumber = 1", nativeQuery = true)
    String oldPaytoPayformDatesIB();

    @Query(value = "select trunc(sysdate) - trunc(datetime) as dateCount\n" +
            "  from (select x.*,\n" +
            "               row_number() over(partition by systemname order by datetime desc) as rownumber\n" +
            "          from tbl_payment_qiwi_balance x\n" +
            "         where x.id_agent = 2\n" +
            "           and x.systemname = 'TNT')\n" +
            " where rownumber = 1", nativeQuery = true)
    Integer oldPaytoPayformCountTNT();

    @Query(value = "select\n" +
            "       (case\n" +
            "         when trunc(sysdate) - trunc(datetime) > 1 then\n" +
            "          substr(datetime, 0, 2) || '-' || substr(trunc(sysdate), 0, 2)  ||\n" +
            "          substr(to_char(trunc(sysdate), 'dd.mm.yyyy'), 3, 8)\n" +
            "         else\n" +
            "          to_char(trunc(datetime), 'dd.mm.yyyy' )\n" +
            "       end) as dates\n" +
            "  from (select x.*,\n" +
            "               row_number() over(partition by systemname order by datetime desc) as rownumber\n" +
            "          from tbl_payment_qiwi_balance x\n" +
            "         where x.id_agent = 2\n" +
            "           and x.systemname = 'TNT')\n" +
            " where rownumber = 1", nativeQuery = true)
    String oldPaytoPayformDatesTNT();

    @Query(value = "select trunc(sysdate) - trunc(datetime) as dateCount\n" +
            "  from (select x.*,\n" +
            "               row_number() over(partition by systemname order by datetime desc) as rownumber\n" +
            "          from tbl_payment_qiwi_balance x\n" +
            "         where x.id_agent = 2\n" +
            "           and x.systemname = 'IBCNP')\n" +
            " where rownumber = 1", nativeQuery = true)
    Integer oldPaytoPayformCountIBCNP();

    @Query(value = "select\n" +
            "       (case\n" +
            "         when trunc(sysdate) - trunc(datetime) > 1 then\n" +
            "          substr(datetime, 0, 2) || '-' || substr(trunc(sysdate), 0, 2)  ||\n" +
            "          substr(to_char(trunc(sysdate), 'dd.mm.yyyy'), 3, 8)\n" +
            "         else\n" +
            "          to_char(trunc(datetime), 'dd.mm.yyyy' )\n" +
            "       end) as dates\n" +
            "  from (select x.*,\n" +
            "               row_number() over(partition by systemname order by datetime desc) as rownumber\n" +
            "          from tbl_payment_qiwi_balance x\n" +
            "         where x.id_agent = 2\n" +
            "           and x.systemname = 'IBCNP')\n" +
            " where rownumber = 1", nativeQuery = true)
    String oldPaytoPayformDatesIBCNP();

}
