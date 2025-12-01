package com.waseeq.BFH.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class GenerateWebhookResponse {

    private String webhook;

    @JsonAlias({ "accessToken", "token" })
    private String accessToken;
}
