package kz.ufo.jobs;

import kz.ufo.entity.TblScheduleTime;
import kz.ufo.repository.TblScheduleTimeRepository;
import kz.ufo.service.CoreLogicMethodsImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class InProcessingTranRepayComplex implements Job {
    @Autowired
    TblScheduleTimeRepository tblScheduleTimeRepository;

    @Autowired
    CoreLogicMethodsImpl coreLogicMethods;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            TblScheduleTime inProcessingTranRepay = tblScheduleTimeRepository.findByName("inProcessingTranRepayComplex");
            if(inProcessingTranRepay.getEnabled()==1) {
                coreLogicMethods.inProcessingTransactionsRepayComplex();
            }
        }catch (Exception e){
            log.error("Error in inProcessingTransactionsRepayComplex",e);

        }
    }

}
