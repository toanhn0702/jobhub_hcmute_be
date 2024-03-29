package vn.iotstar.jobhub_hcmute_be.dto;

import lombok.Data;

@Data
public class CourseDTO {

    String id;

    String title;

    String location;

    String yearBegin;

    String yearEnd;

    String description;

    Boolean isEdit = false;

    Boolean isDelete = false;
}
