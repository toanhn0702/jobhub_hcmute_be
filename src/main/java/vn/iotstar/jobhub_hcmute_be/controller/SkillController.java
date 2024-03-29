package vn.iotstar.jobhub_hcmute_be.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.jobhub_hcmute_be.dto.GenericResponse;
import vn.iotstar.jobhub_hcmute_be.enums.ErrorCodeEnum;
import vn.iotstar.jobhub_hcmute_be.model.ActionResult;
import vn.iotstar.jobhub_hcmute_be.model.ResponseBuild;
import vn.iotstar.jobhub_hcmute_be.model.ResponseModel;
import vn.iotstar.jobhub_hcmute_be.service.SkillService;

@RestController
@RequestMapping("/api/v1/skills")
@Validated
@Tag(name = "Skill", description = "Skill API")
public class SkillController
{
    final
    SkillService skillService;

    @Autowired
    ResponseBuild responseBuild;


    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("")
    public ResponseModel getAllSKills() {
        ActionResult actionResult = new ActionResult();
        try {
            actionResult = skillService.getAllSkill();
        } catch (Exception e) {
            actionResult.setErrorCode(ErrorCodeEnum.BAD_REQUEST);
        }
        return responseBuild.build(actionResult);
    }

}
