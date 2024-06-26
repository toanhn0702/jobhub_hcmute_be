package vn.iotstar.jobhub_hcmute_be.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import vn.iotstar.jobhub_hcmute_be.dto.GenericResponse;
import vn.iotstar.jobhub_hcmute_be.dto.JobDTO;
import vn.iotstar.jobhub_hcmute_be.dto.JobUpdateRequest;
import vn.iotstar.jobhub_hcmute_be.dto.PostJobRequest;
import vn.iotstar.jobhub_hcmute_be.entity.Job;
import vn.iotstar.jobhub_hcmute_be.enums.ErrorCodeEnum;
import vn.iotstar.jobhub_hcmute_be.model.ActionResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JobService {


    List<Job> findAll() throws InterruptedException;


    Job updateJob(Job job);



    <S extends Job> S save(S entity);

    Optional<Job> findById(String s) throws InterruptedException;

    long count();

    void deleteById(String s);

    void delete(Job entity);

    void deleteAll();

    List<Job> findAll(Sort sort);

    Page<Job> findAll(Pageable pageable);

    ActionResult getDetail(String jobId);

    ActionResult getDetail(String jobId, String userId);

    ActionResult findAllByEmployer(String id, Pageable pageable, Boolean isActive);

    Page<Job> findAllByIsActiveOrderByCreatedAtDesc(Boolean isActive, Pageable pageable);

    List<Job> findAllByIsActive(Boolean isActive);


    ActionResult getAllJobs(int index, int size, Boolean isActive);

    ActionResult getAlls(Boolean isActive);

    ActionResult postJob(PostJobRequest jobRequest, String recruiterId);

    ActionResult updateJob(String jobId, JobUpdateRequest jobUpdateRequest, String employerId);

    ActionResult getAllJobsByFilters(String name, String posName, String location, Pageable pageable);

    ActionResult changeStateJob(String jobId);

    ActionResult employerChangeStateJob(String userId, String jobId);
}
