package vn.iotstar.jobhub_hcmute_be.service;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import vn.iotstar.jobhub_hcmute_be.dto.LinkCV;
import vn.iotstar.jobhub_hcmute_be.dto.PutResumeApplyDTO;
import vn.iotstar.jobhub_hcmute_be.entity.Job;
import vn.iotstar.jobhub_hcmute_be.model.ActionResult;

public interface RecommendationService {


    ActionResult getJobRecommendationJobsCv(LinkCV linkCV);

    ActionResult getRecommendationByUserId(String userId, Integer page, Integer size);

    Page<Job> getRecommendationJob(String userId, Integer page, Integer size);

    ActionResult getRecommendationByJobId(String jobId, Integer page, Integer size);


    ActionResult getRecommendationBJobSimilar(String jobId, Integer page, Integer size);

    ActionResult getRecommendUserByJobId(String jobId, int noOfCv);

    ActionResult getRecommendUserByJobApplicant(String jobId, int no_of_cv);

    ActionResult getLinkCVAndSaveToMongo(String linkCV, String userId);
}
