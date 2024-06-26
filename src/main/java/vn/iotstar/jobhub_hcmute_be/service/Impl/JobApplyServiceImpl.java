package vn.iotstar.jobhub_hcmute_be.service.Impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vn.iotstar.jobhub_hcmute_be.constant.State;
import vn.iotstar.jobhub_hcmute_be.dto.Apply.JobApplyResponseDTO;
import vn.iotstar.jobhub_hcmute_be.dto.JobApplyDto;
import vn.iotstar.jobhub_hcmute_be.dto.JobApplyResponse;
import vn.iotstar.jobhub_hcmute_be.dto.PutResumeApplyDTO;
import vn.iotstar.jobhub_hcmute_be.entity.Job;
import vn.iotstar.jobhub_hcmute_be.entity.JobApply;
import vn.iotstar.jobhub_hcmute_be.entity.ResumeUpload;
import vn.iotstar.jobhub_hcmute_be.entity.Student;
import vn.iotstar.jobhub_hcmute_be.enums.ErrorCodeEnum;
import vn.iotstar.jobhub_hcmute_be.model.ActionResult;
import vn.iotstar.jobhub_hcmute_be.repository.JobApplyRepository;
import vn.iotstar.jobhub_hcmute_be.repository.JobRepository;
import vn.iotstar.jobhub_hcmute_be.repository.StudentRepository;
import vn.iotstar.jobhub_hcmute_be.service.JobApplyService;
import vn.iotstar.jobhub_hcmute_be.service.NotificationService;
import vn.iotstar.jobhub_hcmute_be.service.RecommendationService;

import java.util.ArrayList;
import java.util.Date;

import java.util.*;
import java.util.function.Function;

@Service
@Transactional
public class JobApplyServiceImpl implements JobApplyService {

    @Autowired
    JobApplyRepository jobApplyRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    NotificationService notificationService;

    @Autowired
    RecommendationServiceImpl recommendationService;

    @Autowired
    private WebClient webClient;

    public Page<JobApply> findAllByStudent_UserIdOrderByCreatedAtDesc(Pageable pageable, String userId) {
        return jobApplyRepository.findAllByStudent_UserIdOrderByCreatedAtDesc(pageable, userId);
    }

    @Override
    public <S extends JobApply> List<S> saveAll(Iterable<S> entities) {
        return jobApplyRepository.saveAll(entities);
    }

    @Override
    public List<JobApply> findAll() {
        return jobApplyRepository.findAll();
    }

    @Override
    public List<JobApply> findAllById(Iterable<String> strings) {
        return jobApplyRepository.findAllById(strings);
    }

    @Override
    public <S extends JobApply> S save(S entity) {
        return jobApplyRepository.save(entity);
    }

    @Override
    public Optional<JobApply> findById(String s) {
        return jobApplyRepository.findById(s);
    }

    @Override
    public boolean existsById(String s) {
        return jobApplyRepository.existsById(s);
    }

    @Override
    public long count() {
        return jobApplyRepository.count();
    }

    @Override
    public void deleteById(String s) {
        jobApplyRepository.deleteById(s);
    }

    @Override
    public void delete(JobApply entity) {
        jobApplyRepository.delete(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {
        jobApplyRepository.deleteAllById(strings);
    }

    @Override
    public void deleteAll(Iterable<? extends JobApply> entities) {
        jobApplyRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        jobApplyRepository.deleteAll();
    }

    @Override
    public List<JobApply> findAll(Sort sort) {
        return jobApplyRepository.findAll(sort);
    }

    @Override
    public Page<JobApply> findAll(Pageable pageable) {
        return jobApplyRepository.findAll(pageable);
    }

    @Override
    public <S extends JobApply> Page<S> findAll(Example<S> example, Pageable pageable) {
        return jobApplyRepository.findAll(example, pageable);
    }

    @Override
    public <S extends JobApply> long count(Example<S> example) {
        return jobApplyRepository.count(example);
    }

    @Override
    public <S extends JobApply, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return jobApplyRepository.findBy(example, queryFunction);
    }

    @Override
    public ActionResult applyForJob(String userId, String jobId, String resumeUploadId) {
        ActionResult actionResult = new ActionResult();

        // Kiểm tra xem công việc có tồn tại hay không
        Optional<Job> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            actionResult.setErrorCode(ErrorCodeEnum.JOB_NOT_FOUND);
            return actionResult;
        }
        Job job = optionalJob.get();

        // Kiểm tra xem công việc còn hạn apply hay không
        Date expirationDate = job.getDeadline();
        Date currentDate = new Date();
        boolean isJobActive = job.getIsActive();

        if ((isJobActive && expirationDate.before(currentDate)) || !isJobActive) {
            // Nếu công việc đang active nhưng đã hết hạn apply, set trạng thái isActive thành false
            job.setIsActive(false);
            jobRepository.save(job);
            actionResult.setErrorCode(ErrorCodeEnum.JOB_EXPIRED);
            return actionResult;
        }

        // Tìm ứng viên trong cơ sở dữ liệu
        Optional<Student> optionalCandidate = studentRepository.findById(userId);
        if (optionalCandidate.isEmpty()) {
            actionResult.setErrorCode(ErrorCodeEnum.CANDIDATE_NOT_FOUND);
            return actionResult;
        }
        Student candidate = optionalCandidate.get();

        Optional<JobApply> optionalJobApply = jobApplyRepository.findByStudentAndJob(candidate, job);
        // Kiểm tra xem ứng viên đã apply vào công việc này chưa
        if (optionalJobApply.isPresent()) {
            actionResult.setErrorCode(ErrorCodeEnum.ALREADY_APPLY);
            return actionResult;
        }

        // Kiểm tra xem CV của ứng viên tồn tại
        boolean isResumeFound = candidate.getResume().getResumeUploads()
                .stream()
                .anyMatch(upload -> upload.getResumeId().equals(resumeUploadId));
        String resumeLink = "";
        if (isResumeFound) {
            Optional<ResumeUpload> optionalResumeUpload = candidate.getResume().getResumeUploads()
                    .stream()
                    .filter(upload -> upload.getResumeId().equals(resumeUploadId))
                    .findFirst();
            if (optionalResumeUpload.isPresent()) {
                resumeLink = optionalResumeUpload.get().getLinkUpload();
            }
        }
        if (!isResumeFound) {
            actionResult.setErrorCode(ErrorCodeEnum.CV_NOT_FOUND);
            return actionResult;
        }

        // Tạo đơn ứng tuyển
        JobApply application = new JobApply();
        application.setJob(job);
        application.setState(State.PENDING);
        application.setStudent(candidate);
        application.setResume(candidate.getResume());
        application.setResumeUpoad(resumeLink);
        // Ánh xạ những thứ còn lại của candidate qua cho JobApply
        BeanUtils.copyProperties(candidate, application);

        // Lưu đơn ứng tuyển vào database
        JobApply jobApply = jobApplyRepository.save(application);

        PutResumeApplyDTO dto = new PutResumeApplyDTO();
        dto.setJob_id(jobId);
        dto.setUser_id(userId);
        dto.setLink_cv(resumeLink);
        // String result = String.valueOf(putCVApplyToMongo(dto));

        JobApplyResponse jobApplyResponse = JobApplyResponse.transform(jobApply);

        actionResult.setData(jobApplyResponse);
        actionResult.setErrorCode(ErrorCodeEnum.APPLICATION_SUCCESSFULLY);
//        notificationService.sendApplyJob(job.getEmployer().getEmail(), candidate.getFullName(), job.getName());
        return actionResult;
    }


    @Override
    public ActionResult findJobAppliesByCandidate(String studentId, Pageable pageable) {

        ActionResult actionResult = new ActionResult();
        Page<JobApply> jobApplyPage = findAllByStudent_UserIdOrderByCreatedAtDesc(pageable, studentId);
        List<JobApplyDto> jobApplyDtos = new ArrayList<>();
        for (JobApply jobApply : jobApplyPage.getContent()) {
            JobApplyDto jobApplyDto = new JobApplyDto();
            BeanUtils.copyProperties(jobApply, jobApplyDto);
            jobApplyDto.setLocationJob(jobApply.getJob().getLocation());
            jobApplyDto.setLogoJob(jobApply.getJob().getLogo());
            jobApplyDto.setNameJob(jobApply.getJob().getName());
            jobApplyDto.setJobId(jobApply.getJob().getJobId());
            jobApplyDtos.add(jobApplyDto);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content", jobApplyDtos);
        map.put("pageNumber", jobApplyPage.getPageable().getPageNumber());
        map.put("pageSize", jobApplyPage.getSize());
        map.put("totalPages", jobApplyPage.getTotalPages());
        map.put("totalElements", jobApplyPage.getTotalElements());
        actionResult.setData(map);
        actionResult.setErrorCode(ErrorCodeEnum.GET_JOB_APPLY_SUCCESSFULLY);

        return actionResult;

    }

    @Override
    public Page<JobApply> findAllByUserIdAndDateFilters(Pageable pageable, String userId, int days, String state) {
        Date endDate = new Date();
        Date startDate = calculateStartDate(endDate, days);
        if (state.equals("ALL"))
            return jobApplyRepository.findAllByJob_Employer_UserIdAndCreatedAtBetween(pageable, userId, startDate, endDate);
        else
            return jobApplyRepository.findAllByJob_Employer_UserIdAndCreatedAtBetweenAndState(pageable, userId, startDate, endDate, State.getStatusName(state));
    }

    @Override
    public ActionResult getAllByUserIdAndDateFilters(Pageable pageable, String userId, int days, String state) {
        ActionResult actionResult = new ActionResult();
        Page<JobApply> jobApplyPage = findAllByUserIdAndDateFilters(pageable, userId, days, state);

        List<JobApplyResponseDTO> jobApplyDtos = new ArrayList<>();

        for (JobApply jobApply : jobApplyPage.getContent()) {
            JobApplyResponseDTO jobApplyDto = new JobApplyResponseDTO();
            BeanUtils.copyProperties(jobApply, jobApplyDto);
            BeanUtils.copyProperties(jobApply.getJob(), jobApplyDto);
            BeanUtils.copyProperties(jobApply.getStudent(), jobApplyDto);
            jobApplyDtos.add(jobApplyDto);
        }


        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content", jobApplyDtos);
        map.put("pageNumber", jobApplyPage.getPageable().getPageNumber());
        map.put("pageSize", jobApplyPage.getSize());
        map.put("totalPages", jobApplyPage.getTotalPages());
        map.put("totalElements", jobApplyPage.getTotalElements());
        actionResult.setData(map);
        actionResult.setErrorCode(ErrorCodeEnum.GET_JOB_APPLY_SUCCESSFULLY);
        return actionResult;
    }

    private Date calculateStartDate(Date endDate, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    @Override
    public ActionResult getAllByJobIdAndEmployerId(Pageable pageable, String jobId, String userId, String state) {
        ActionResult actionResult = new ActionResult();
        Page<JobApply> jobApplyPage;
        if (state.equals("ALL"))
            jobApplyPage = jobApplyRepository.findAllByJob_JobIdAndJob_Employer_UserId(pageable, jobId, userId);
        else
            jobApplyPage = jobApplyRepository.findAllByJob_JobIdAndJob_Employer_UserIdAndState(pageable, jobId, userId, State.getStatusName(state));

        List<JobApplyResponseDTO> jobApplyDtos = new ArrayList<>();

        for (JobApply jobApply : jobApplyPage.getContent()) {
            JobApplyResponseDTO jobApplyDto = new JobApplyResponseDTO();
            BeanUtils.copyProperties(jobApply, jobApplyDto);
            BeanUtils.copyProperties(jobApply.getJob(), jobApplyDto);
            BeanUtils.copyProperties(jobApply.getStudent(), jobApplyDto);
            jobApplyDtos.add(jobApplyDto);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content", jobApplyDtos);
        map.put("pageNumber", jobApplyPage.getPageable().getPageNumber());
        map.put("pageSize", jobApplyPage.getSize());
        map.put("totalPages", jobApplyPage.getTotalPages());
        map.put("totalElements", jobApplyPage.getTotalElements());
        actionResult.setData(map);
        actionResult.setErrorCode(ErrorCodeEnum.GET_JOB_APPLY_SUCCESSFULLY);
        return actionResult;
    }


}
