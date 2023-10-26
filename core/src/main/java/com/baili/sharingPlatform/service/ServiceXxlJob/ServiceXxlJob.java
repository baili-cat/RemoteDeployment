package com.baili.sharingPlatform.service.ServiceXxlJob;

import com.baili.sharingPlatform.service.ServiceXxlJob.model.JobInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author baili
 * @date 2023年07月28日15:04
 */
@Service
public class ServiceXxlJob {
    //TODO 用不到了没有测
    private XxlJobAdminManager xxlJobAdminManager;

    public Boolean addJob(XxlJobProperties xxlJobProperties, ArrayList<JobInfo> jobInfos){
        try {
            xxlJobAdminManager = new XxlJobAdminManager(xxlJobProperties);
            for(JobInfo  jobInfo : jobInfos) {
                xxlJobAdminManager.addJob(jobInfo);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

}
