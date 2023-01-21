package kz.ufo.jobs;

import kz.ufo.entity.TblScheduleTime;
import kz.ufo.repository.TblScheduleTimeRepository;
import kz.ufo.service.CoreLogicMethodsImpl;
import kz.ufo.service.EmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class UpdatePayStatus implements Job {

   @Autowired
    TblScheduleTimeRepository tblScheduleTimeRepository;

    @Autowired
    CoreLogicMethodsImpl coreLogicMethods;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            TblScheduleTime scheduleTime = tblScheduleTimeRepository.findByName("updatePayStatus");
            if(scheduleTime.getEnabled()==1) {
                coreLogicMethods.checkAndUpdateStatus();
            }
        }catch (Exception e){
            log.error("Error in updatePayStatus",e);

        }
    }
}
