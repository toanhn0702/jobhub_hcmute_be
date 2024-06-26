package vn.iotstar.jobhub_hcmute_be.service.Impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.jobhub_hcmute_be.constant.Gender;
import vn.iotstar.jobhub_hcmute_be.dto.GenericResponse;
import vn.iotstar.jobhub_hcmute_be.dto.UserUpdateRequest;
import vn.iotstar.jobhub_hcmute_be.entity.Student;
import vn.iotstar.jobhub_hcmute_be.enums.ErrorCodeEnum;
import vn.iotstar.jobhub_hcmute_be.model.ActionResult;
import vn.iotstar.jobhub_hcmute_be.repository.InterviewRepository;
import vn.iotstar.jobhub_hcmute_be.repository.JobApplyRepository;
import vn.iotstar.jobhub_hcmute_be.repository.ResumeUploadRepository;
import vn.iotstar.jobhub_hcmute_be.repository.StudentRepository;
import vn.iotstar.jobhub_hcmute_be.service.CloudinaryService;
import vn.iotstar.jobhub_hcmute_be.service.StudentService;
import vn.iotstar.jobhub_hcmute_be.utils.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl extends RedisServiceImpl implements StudentService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    JobApplyRepository jobApplyRepository;

    @Autowired
    InterviewRepository interviewRepository;

    @Autowired
    ResumeUploadRepository resumeUploadRepository;

    public StudentServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public List<Student> findAllById(Iterable<String> strings) {
        return studentRepository.findAllById(strings);
    }

    @Override
    public <S extends Student> S save(S entity) {
        return studentRepository.save(entity);
    }

    @Override
    public Optional<Student> findById(String s) {
        return studentRepository.findById(s);
    }

    @Override
    public boolean existsById(String s) {
        return studentRepository.existsById(s);
    }

    @Override
    public long count() {
        return studentRepository.count();
    }

    @Override
    public void deleteById(String s) {
        studentRepository.deleteById(s);
    }

    @Override
    public void delete(Student entity) {
        studentRepository.delete(entity);
    }

    @Override
    public void deleteAll() {
        studentRepository.deleteAll();
    }

    @Override
    public ResponseEntity<GenericResponse> changeAvatar(String userId, MultipartFile imageFile) throws IOException {

        Student user = findById(userId).get();
        String avatarOld = user.getAvatar();

        //upload new avatar
        user.setAvatar(cloudinaryService.uploadImage(imageFile));
        save(user);

        //delete old avatar
        if (avatarOld != null) {
            cloudinaryService.deleteImage(avatarOld);
        }
        return ResponseEntity.ok(GenericResponse.builder()
                .success(true)
                .message("Upload successfully")
                .result(user.getAvatar())
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @Override
    public ResponseEntity<GenericResponse> getProfile(String userId){
        Optional<Student> optional = findById(userId);
        if (optional.isEmpty())
            throw new RuntimeException("User not found");
        Student student = optional.get();

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Retrieving student profile successfully")
                        .result(student)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }


    @Override
    public ActionResult updateProfile(String userId, UserUpdateRequest request) throws Exception {
        ActionResult actionResult = new ActionResult();
        Optional<Student> user = findById(userId);
        String phone =  request.getPhone();
        if (user.isEmpty()){
            actionResult.setErrorCode(ErrorCodeEnum.USER_NOT_FOUND);
            return actionResult;
        }
//        if(!phone.isEmpty()){
//            Optional<Student> optional = studentRepository.findByPhoneAndIsActiveIsTrue(phone);
//            if(optional.isPresent() && !optional.get().getUserId().equals(userId))
//            {
//                actionResult.setErrorCode(ErrorCodeEnum.PHONE_EXISTED);
//                return actionResult;
//            }
//        }
        user.get().setGender(Gender.valueOf(String.valueOf(request.getGender())));
        user.get().setPhone(phone);
        user.get().setFullName(request.getFullName());
        user.get().setDateOfBirth(request.getDateOfBirth());
        user.get().setAddress(request.getAddress());
        user.get().setAbout(request.getAbout());
        save(user.get());
        this.delete(Constants.USERS, userId);
        if (this.exists(Constants.USERS)) this.delete(Constants.USERS);
        actionResult.setData(user.get());
        actionResult.setErrorCode(ErrorCodeEnum.UPDATE_PROFILE_SUCCESS);
        return actionResult;
    }

    @Override
    public ActionResult getDashBoard(String userId){
        ActionResult actionResult = new ActionResult();

        try{
            Optional<Student> user = findById(userId);
            if (user.isEmpty()) {
                actionResult.setErrorCode(ErrorCodeEnum.USER_NOT_FOUND);
                return actionResult;
            }
            Long totalJobApply = jobApplyRepository.countByStudent_UserId(userId);
            Long totalInterview = interviewRepository.countByJobApply_Student_UserId(userId);
            Long totalShortList = jobApplyRepository.countByStudent_UserId(userId);
            Long totalResumeUpload = resumeUploadRepository.countByResume_Student_UserIdAndIsActiveIsTrue(userId);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("totalJobApply", totalJobApply);
            map.put("totalInterview", totalInterview);
            map.put("totalShortList", totalShortList);
            map.put("totalResumeUpload", totalResumeUpload);
            actionResult.setData(map);
            actionResult.setErrorCode(ErrorCodeEnum.GET_DASHBOARD_SUCCESS);

        }catch (Exception e){
            actionResult.setErrorCode(ErrorCodeEnum.BAD_REQUEST);
            System.err.println(e.getMessage());
        }

        return actionResult;
    }
}
