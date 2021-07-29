package com.taptap.tds.registration.server.job;

import com.taptap.tds.registration.server.service.PublicityService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class CheckIdentificationJob implements Job {

    @Autowired
    private PublicityService publicityService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            publicityService.check();
        } catch (Throwable e) {
            throw new org.quartz.JobExecutionException(e);
        }
    }
}
