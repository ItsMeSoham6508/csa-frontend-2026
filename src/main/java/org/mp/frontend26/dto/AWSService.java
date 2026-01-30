package org.mp.frontend26.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AWSService {
    public Integer id;
    public String serviceName;
    public String category;
    public String serviceDescription;
    public Boolean freeTierEligibility;
    public String documentationUrl;
}