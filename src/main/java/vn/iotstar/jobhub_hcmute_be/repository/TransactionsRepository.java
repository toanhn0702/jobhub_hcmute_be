package vn.iotstar.jobhub_hcmute_be.repository;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.iotstar.jobhub_hcmute_be.entity.Employer;
import vn.iotstar.jobhub_hcmute_be.entity.Transactions;

import java.util.List;
import java.util.Optional;

@Hidden
@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, String> {

    Optional<Transactions> findByEmployerAndStatus(Employer employer, String pending);

    Page<Transactions> findAllByEmployer(Employer employer, Pageable pageable);

    Long countByIdIsNotNull();

    long countByCodeAndStatus(String code, String status);


    @Query("SELECT SUM(t.amount) FROM Transactions t WHERE t.code = :code AND t.status = :status")
    Long calculateTotalAmountByCodeAndStatus(String code, String status);


    @Query("SELECT SUM(t.amount) FROM Transactions t WHERE t.status = :status")
    Long calculateTotalAmountByStatus(String status);

    long countByStatus(String status);

    Page<Transactions> findAllByEmployer_UserIdAndStatus(String userId, String status, Pageable pageable);

}