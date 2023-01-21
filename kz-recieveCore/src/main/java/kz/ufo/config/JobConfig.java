package kz.ufo.config;

import kz.ufo.entity.TblScheduleTime;
import kz.ufo.jobs.*;
import kz.ufo.repository.TblScheduleTimeRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JobConfig {

    @Autowired
    TblScheduleTimeRepository tblScheduleTimeRepository;



    @Primary
    String getCron(String code){
        TblScheduleTime updateProviders = tblScheduleTimeRepository.findByName(code);
        return updateProviders.getCron();
    }


    @Bean
    public JobDetail jobCheckBalanceQiwiSubagentsDetails() {
        return JobBuilder.newJob(CheckBalanceQiwiSubagents.class).withIdentity("checkBalanceQiwiSubagents")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobCheckBalanceQiwiSubagentsTrigger(JobDetail jobCheckBalanceQiwiSubagentsDetails) {

        return TriggerBuilder.newTrigger().forJob(jobCheckBalanceQiwiSubagentsDetails)

                .withIdentity("checkBalanceQiwiSubagentsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("checkBalanceQiwiSubagents")))
                .build();
    }

    @Bean
    public JobDetail jobInProcessingTranRepayDetails() {
        return JobBuilder.newJob(InProcessingTranRepay.class).withIdentity("inProcessingTranRepay")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobInProcessingTranRepayTrigger(JobDetail jobInProcessingTranRepayDetails) {

        return TriggerBuilder.newTrigger().forJob(jobInProcessingTranRepayDetails)

                .withIdentity("InProcessingTranRepayTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("inProcessingTranRepay")))
                .build();
    }

    @Bean
    public JobDetail jobPayToPayformTransactionsDetails() {
        return JobBuilder.newJob(PayToPayformTransactions.class).withIdentity("payToPayformTransactions")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobPayToPayformTransactionsTrigger(JobDetail jobPayToPayformTransactionsDetails) {

        return TriggerBuilder.newTrigger().forJob(jobPayToPayformTransactionsDetails)

                .withIdentity("payToPayformTransactionsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("payToPayformTransactions")))
                .build();
    }

    @Bean
    public JobDetail jobRepayDetails() {
        return JobBuilder.newJob(Repay.class).withIdentity("repay")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobRepayTrigger(JobDetail jobRepayDetails) {

        return TriggerBuilder.newTrigger().forJob(jobRepayDetails)

                .withIdentity("jobRepayTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("repay")))
                .build();
    }

    @Bean
    public JobDetail jobSendToQueueDetails() {
        return JobBuilder.newJob(SendToQueue.class).withIdentity("sendToQueue")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobSendToQueueTrigger(JobDetail jobSendToQueueDetails) {

        return TriggerBuilder.newTrigger().forJob(jobSendToQueueDetails)

                .withIdentity("sendToQueueTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("sendToQueue")))
                .build();
    }

    @Bean
    public JobDetail jobUpdateActiveProvidersDetails() {
        return JobBuilder.newJob(UpdateActiveProviders.class).withIdentity("updateActiveProviders")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobUpdateActiveProvidersTrigger(JobDetail jobUpdateActiveProvidersDetails) {

        return TriggerBuilder.newTrigger().forJob(jobUpdateActiveProvidersDetails)

                .withIdentity("updateActiveProvidersTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("updateActiveProviders")))
                .build();
    }

    @Bean
    public JobDetail jobUpdatePayStatusDetails() {
        return JobBuilder.newJob(UpdatePayStatus.class).withIdentity("updatePayStatus")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobUpdatePayStatusTrigger(JobDetail jobUpdatePayStatusDetails) {

        return TriggerBuilder.newTrigger().forJob(jobUpdatePayStatusDetails)

                .withIdentity("updatePayStatusTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("updatePayStatus")))
                .build();
    }

    @Bean
    public JobDetail jobUpdateProvidersDetails() {
        return JobBuilder.newJob(UpdateProviders.class).withIdentity("updateProviders")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobUpdateProvidersTrigger(JobDetail jobUpdateProvidersDetails) {

        return TriggerBuilder.newTrigger().forJob(jobUpdateProvidersDetails)

                .withIdentity("updateProvidersTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("updateProviders")))
                .build();
    }

    @Bean
    public JobDetail jobInProcessingTranRepayComplexDetails() {
        return JobBuilder.newJob(InProcessingTranRepayComplex.class).withIdentity("inProcessingTranRepayComplex")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobInProcessingTranRepayComplexTrigger(JobDetail jobInProcessingTranRepayComplexDetails) {

        return TriggerBuilder.newTrigger().forJob(jobInProcessingTranRepayComplexDetails)

                .withIdentity("InProcessingTranRepayComplexTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("inProcessingTranRepayComplex")))
                .build();
    }


    @Bean
    public JobDetail jobUpdatePayStatusComplexDetails() {
        return JobBuilder.newJob(UpdatePayStatusComplex.class).withIdentity("updatePayStatusComplex")
                .storeDurably().build();
    }

    @Bean
    public Trigger jobUpdatePayStatusComplexTrigger(JobDetail jobUpdatePayStatusComplexDetails) {

        return TriggerBuilder.newTrigger().forJob(jobUpdatePayStatusComplexDetails)

                .withIdentity("updatePayStatusComplexTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(getCron("updatePayStatusComplex")))
                .build();
    }



}
