package vn.iotstar.jobhub_hcmute_be.repository;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.jobhub_hcmute_be.entity.Student;
import vn.iotstar.jobhub_hcmute_be.entity.User;

import java.util.List;
import java.util.Optional;

@Hidden
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {


    Optional<Student> findByEmailAndIsActiveIsTrue(String email);

    Optional<Student> findByPhoneAndIsActiveIsTrue(String phone);

    Optional<Student> findByUserIdAndIsActiveIsTrue(String userId);

    Long countByUserIdIsNotNull();


    Page<Student> findByUserIdIn(List<String> list, Pageable pageable);
}