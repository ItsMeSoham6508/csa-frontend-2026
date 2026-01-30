package org.mp.frontend26.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AWSModule {
    public Integer moduleId;
    public String moduleTitle;
    public String moduleSummary;
    public String learningOutcomes;
    public String imageUri;
    public String salesPitch;
    public Float timeRequirement;
    public Double difficultyLevel;
    public List<Example> examples;
}