package vn.iotstar.jobhub_hcmute_be.service.Impl;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import vn.iotstar.jobhub_hcmute_be.constant.EmployState;
import vn.iotstar.jobhub_hcmute_be.dto.*;
import vn.iotstar.jobhub_hcmute_be.dto.Auth.EmployerRegisterDTO;
import vn.iotstar.jobhub_hcmute_be.dto.Auth.LoginDTO;
import vn.iotstar.jobhub_hcmute_be.dto.Auth.RegisterRequest;
import vn.iotstar.jobhub_hcmute_be.entity.*;
import vn.iotstar.jobhub_hcmute_be.exception.UserNotFoundException;
import vn.iotstar.jobhub_hcmute_be.repository.PasswordResetOtpRepository;
import vn.iotstar.jobhub_hcmute_be.repository.RoleRepository;
import vn.iotstar.jobhub_hcmute_be.repository.UserRepository;
import vn.iotstar.jobhub_hcmute_be.repository.VerificationTokenRepository;
import vn.iotstar.jobhub_hcmute_be.security.JwtTokenProvider;
import vn.iotstar.jobhub_hcmute_be.security.UserDetail;
import vn.iotstar.jobhub_hcmute_be.service.CloudinaryService;
import vn.iotstar.jobhub_hcmute_be.service.EmailVerificationService;
import vn.iotstar.jobhub_hcmute_be.service.RefreshTokenService;
import vn.iotstar.jobhub_hcmute_be.service.UserService;

import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EmailVerificationService emailVerificationService;

    @Autowired
    VerificationTokenRepository tokenRepository;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    PasswordResetOtpRepository passwordResetOtpRepository;


    @Override
    @Deprecated
    public User getById(String s) {
        return userRepository.getById(s);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }

    @Override
    public Optional<User> findById(String s) {
        return userRepository.findById(s);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public void deleteById(String s) {
        userRepository.deleteById(s);
    }

    @Override
    public void delete(User entity) {
        userRepository.delete(entity);
    }

    @Override
    public List<User> findAll(Sort sort) {
        return userRepository.findAll(sort);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public ResponseEntity<GenericResponse> getProfile(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new RuntimeException("User not found");

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Retrieving user profile successfully")
                        .result(new UserProfileResponse(user.get()))
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Override
    public ResponseEntity<GenericResponse> userLogin(LoginDTO loginDTO) {

        if (findByEmail(loginDTO.getUserLogin()).isEmpty())
            throw new UserNotFoundException("Account does not exist");
        Optional<User> optionalUser = findByEmail(loginDTO.getUserLogin());
        if (optionalUser.isPresent() && !optionalUser.get().isVerified()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Your account is not verified!")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }

        //Optional<User> optionalUser = findByEmail(loginDTO.getUserLogin());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUserLogin(),
                        loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetail userDetail = (UserDetail) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
        RefreshToken refreshToken = new RefreshToken();
        String token = jwtTokenProvider.generateRefreshToken(userDetail);
        refreshToken.setToken(token);
        refreshToken.setUser(userDetail.getUser());
        //invalid all refreshToken before
        refreshTokenService.revokeRefreshToken(userDetail.getUserId());
        refreshTokenService.save(refreshToken);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", token);
        tokenMap.put("role", userDetail.getUser().getRole().getName());

        if (optionalUser.isPresent()) {
            optionalUser.get().setLastLoginAt(new Date());
            save(optionalUser.get());
        }

        return ResponseEntity.ok().body(GenericResponse.builder()
                .success(true)
                .message("Login successfully!")
                .result(tokenMap)
                .statusCode(HttpStatus.OK.value())
                .build());

    }

    @Override
    public ResponseEntity<GenericResponse> changeUserPassord(User user, PasswordResetRequest request) {
        String oldPass = user.getPassword();
        String oldPassDTO = passwordEncoder.encode(request.getOldPassword());
        if(oldPassDTO.equals(oldPassDTO)){
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user = save(user);
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Change Password successfully!")
                    //.result(user)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }
        return ResponseEntity.status(400)
                .body(
                        GenericResponse.builder()
                                .success(true)
                                .message("Old Password don't match")
                                .result(null)
                                .statusCode(HttpStatus.CONFLICT.value())
                                .build()
                );

    }


    @Override
    public ResponseEntity<?> employerRegister(EmployerRegisterDTO employerRegisterDTO){
        if (employerRegisterDTO.getPassword().length() < 8 || employerRegisterDTO.getPassword().length() > 32)
            throw new RuntimeException("Password must be between 8 and 32 characters long");

        Optional<User> userOptional = findByEmail(employerRegisterDTO.getEmail());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Email already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        if (!employerRegisterDTO.getPassword().equals(employerRegisterDTO.getConfirmPassword()))
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Password and confirm password do not match")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        Employer user = new Employer();
        user.setCompanyName(employerRegisterDTO.getFullName());
        user.setEmail(employerRegisterDTO.getEmail());
        user.setUserId(UUID.randomUUID().toString().split("-")[0]);
        user.setPassword(passwordEncoder.encode(employerRegisterDTO.getPassword()));
        user.setRole(roleRepository.findByName("EMPLOYER"));

        user.setEmployState(EmployState.PENDDING);
        user = save(user);
        emailVerificationService.sendOtpEmployer(employerRegisterDTO.getEmail(), employerRegisterDTO.getFullName());

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Sign Up Success. Please check your email to verify your account!")
                        .result(user)
                        .statusCode(200)
                        .build()
        );
    }


    @Override
    public ResponseEntity<GenericResponse> userRegisterEmail(RegisterRequest registerRequest) {
        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32)
            throw new RuntimeException("Password must be between 8 and 32 characters long");

        Optional<User> userOptional = findByEmail(registerRequest.getEmail());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Email already in use")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Password and confirm password do not match")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        Student user = new Student();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        //user.setUserId(UUID.randomUUID().toString().split("-")[0]);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(roleRepository.findByName("STUDENT"));
        //user.setResume(new Resume());
        user = save(user);
        emailVerificationService.sendOtp(user.getEmail());

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Sign Up Success. Please check your email to verify your account!")
                        .result(user)
                        .statusCode(200)
                        .build()
        );
    }

    @Cacheable("users")
    @Override
    public ResponseEntity<GenericResponse> getAccounts(int size, int page) {
        if (page < 0) {
            return ResponseEntity.ok(GenericResponse.builder()
                    .message("Page index must not be less than 0")
                    .statusCode(500)
                    .success(false)
                    .build());
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<User> users = findAll(pageable);

        return getGenericResponseResponseEntity(users);
    }

    @NotNull
    private ResponseEntity<GenericResponse> getGenericResponseResponseEntity(Page<User> users) {
        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("Get user successful")
                        .result(response)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @Cacheable(key = "#role", value = "users")
    @Override
    public ResponseEntity<GenericResponse> getAccounts(String role, int size, int page) throws Exception {
        if (page < 0) {
            return ResponseEntity.ok(GenericResponse.builder()
                    .message("Page index must not be less than 0")
                    .statusCode(500)
                    .success(false)
                    .build());
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<User> users;
        Role roleUser = roleRepository.findByName(role);
        if(role.isEmpty()){
            users = findAll(pageable);
            return getGenericResponseResponseEntity(users);
        }
        if(roleUser == null){
            return ResponseEntity.ok(GenericResponse.builder()
                    .message("Role not found")
                    .statusCode(500)
                    .success(false)
                    .build());
        }
        users = userRepository.findByRole_NameAndIsActiveIsTrue(role, pageable);

        return getGenericResponseResponseEntity(users);
    }

    @Override
    public ResponseEntity<GenericResponse> validateVerificationAccount(String token) {

//        String message = "Invalid token, please check the token again!";
//        EmailVerification emailVerification = emailVerificationService.findByToken(token);
//        if (verificationToken != null) {
//            message = "Account verification successful, please login!";
//            Context context = new Context();
//            context.setVariable("result", message);
//            String content = templateEngine.process("result-confirm", context);
//            User user = verificationToken.getUser();
//            user.setVerified(true);
//            user = userRepository.save(user);
//        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(GenericResponse.builder()
                        .success(true)
                        .message("message")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @Override
    public void createPasswordResetOtpForUser(User user, String otp) {
        PasswordResetOtp myOtp = null;

        Optional<PasswordResetOtp> optionalUser = passwordResetOtpRepository.findByUser(user);
        if (optionalUser.isPresent()) {
            myOtp = passwordResetOtpRepository.findByUser(user).get();
            myOtp.updateOtp(otp);
        } else {

            myOtp = new PasswordResetOtp(otp, user);
        }
        passwordResetOtpRepository.save(myOtp);
    }

    @Override
    public String validatePasswordResetOtp(String otp) {

        Optional<PasswordResetOtp> passOtp = passwordResetOtpRepository.findByOtp(otp);
        Calendar cal = Calendar.getInstance();

        if (passOtp.isEmpty()) {
            return "Invalid token/link";
        }
        if (passOtp.get().getExpiryDate().before(cal.getTime())) {
            return "Token/link expired";
        }
        return null;

    }

    @Override
    public Optional<PasswordResetOtp> getUserByPasswordResetOtp(String otp) {
        return passwordResetOtpRepository.findByOtp(otp);
    }

    @Override
    public void changeUserPassword(User user, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword))
            throw new RuntimeException("Password and confirm password do not match");
        user.setPassword(passwordEncoder.encode(newPassword));
        save(user);
    }

//
//


}

