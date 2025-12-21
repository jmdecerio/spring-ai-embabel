package com.jmdecerio.spring_ai_embabel.controllers;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.jmdecerio.spring_ai_embabel.model.Answer;
import com.jmdecerio.spring_ai_embabel.model.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final AgentPlatform agentPlatform;

    @PostMapping("/question")
    public Answer gameMechanics(@RequestBody Question question) {
        return AgentInvocation.create(agentPlatform, Answer.class)
                .invoke(new UserInput(question.question()));
    }

}
