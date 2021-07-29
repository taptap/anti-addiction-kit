package com.taptap.tds.registration.server.job;

import com.taptap.tds.registration.server.service.PublicityService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class UploadUserActionJob  implements Job {

    @Autowired
    private PublicityService publicityService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            publicityService.uploadUserActionToPublicity();
        } catch (Throwable e) {
            throw new org.quartz.JobExecutionException(e);
        }
    }
}
