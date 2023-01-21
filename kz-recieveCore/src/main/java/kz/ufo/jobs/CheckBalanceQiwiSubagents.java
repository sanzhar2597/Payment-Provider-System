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
public class CheckBalanceQiwiSubagents implements Job {

    @Autowired
    CoreLogicMethodsImpl coreLogicMethods;

    @Autowired
    TblScheduleTimeRepository tblScheduleTimeRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            TblScheduleTime checkBalQiwiSubagents = tblScheduleTimeRepository.findByName("checkBalanceQiwiSubagents");
            if(checkBalQiwiSubagents.getEnabled()==1) {
                coreLogicMethods.checkBalanceSubAgents();
            }
        }catch (Exception e){
            log.error("Error in checkBalanceQiwiSubagents",e);
        }
    }
}
