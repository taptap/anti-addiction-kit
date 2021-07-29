package com.taptap.tds.registration.server.configuration;

import com.taptap.tds.registration.server.job.CheckIdentificationJob;
import com.taptap.tds.registration.server.job.UploadUserActionJob;
import org.quartz.JobDetail;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.concurrent.TimeUnit;

@Configuration
public class QuartzConfiguration {

    @Bean
    public SchedulerFactoryBeanCustomizer enableOverwrite() {
        return schedulerFactoryBean -> schedulerFactoryBean.setOverwriteExistingJobs(true);
    }

    @Bean
    public JobDetailFactoryBean checkIdentificationJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(CheckIdentificationJob.class);
        jobDetailFactoryBean.setDurability(true);
        return jobDetailFactoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean checkIdentificationJobTrigger(JobDetail checkIdentificationJob) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(checkIdentificationJob);
        triggerFactoryBean.setStartDelay(TimeUnit.SECONDS.toMillis(10));
        triggerFactoryBean.setRepeatInterval(TimeUnit.SECONDS.toMillis(10));
        return triggerFactoryBean;
    }

    @Bean
    public JobDetailFactoryBean uploadUserActionJob() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(UploadUserActionJob.class);
        jobDetailFactoryBean.setDurability(true);
        return jobDetailFactoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean uploadUserActionJobTrigger(JobDetail uploadUserActionJob) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(uploadUserActionJob);
        triggerFactoryBean.setStartDelay(TimeUnit.SECONDS.toMillis(10));
        triggerFactoryBean.setRepeatInterval(TimeUnit.SECONDS.toMillis(10));
        return triggerFactoryBean;
    }

}
