package com.waseeq.BFH.runner;

import com.waseeq.BFH.service.QualifierFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final QualifierFlowService flowService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        flowService.executeOnce();
    }
}
