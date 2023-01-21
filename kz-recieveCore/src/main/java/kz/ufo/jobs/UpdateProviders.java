package kz.ufo.jobs;

import kz.ufo.entity.TblScheduleTime;
import kz.ufo.repository.TblScheduleTimeRepository;
import kz.ufo.service.EmailServiceImpl;
import kz.ufo.service.PayFServiceImpl;
import kz.ufo.service.QiwiSoapServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class UpdateProviders implements Job {

    @Autowired
    QiwiSoapServiceImpl qiwiSoapService;

    @Autowired
    PayFServiceImpl payFService;

    @Autowired
    TblScheduleTimeRepository tblScheduleTimeRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            TblScheduleTime scheduleTime = tblScheduleTimeRepository.findByName("updateProviders");
            if(scheduleTime.getEnabled()==1) {

                qiwiSoapService.getAndSaveProviderList();


                log.info("Успешно обновлены провайдеры Киви");
            }
        }catch (Exception e){
            log.error("Error in updateProviders Киви",e);

        }
        try {
            TblScheduleTime scheduleTime = tblScheduleTimeRepository.findByName("updateProviders");
            if(scheduleTime.getEnabled()==1) {

                payFService.getAndSavePayFormServices();

                log.info("Успешно обновлены провайдеры Пэйформ");
            }
        }catch (Exception e){
            log.error("Error in updateProviders Пэйформ",e);

        }
    }
}
