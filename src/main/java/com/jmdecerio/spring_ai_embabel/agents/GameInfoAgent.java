package com.jmdecerio.spring_ai_embabel.agents;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.jmdecerio.spring_ai_embabel.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Agent( name = "GameInfoAgent",
        description = "An agent that helps users answer questions " +
                "about board games, including mechanics and player counts.",
        version = "1.0.0")
public class GameInfoAgent {

    @Value("classpath:/promptTemplates/mechanicsDeterminer.st")
    Resource mechanicsDeterminerPromptTemplate;

    @Value("classpath:/promptTemplates/playerCount.st")
    Resource playerCountPromptTemplate;

    @Value("classpath:/promptTemplates/rulesFetcher.st")
    Resource rulesFetcherPromptTemplate;

    @Value("classpath:/promptTemplates/determineTitle.st")
    Resource determineTitlePromptTemplate;

    @Value("classpath:/promptTemplates/intentClassifier.st")
    Resource intentClassifierPromptTemplate;

    private final String rulesFilePath;

    public GameInfoAgent(
            @Value("${boardgame.rules.path}") String rulesFilePath) {
        this.rulesFilePath = rulesFilePath;
    }

    @Action
    @AchievesGoal(description = "Game mechanics have been determined.",
            export = @Export(
                    name = "gameMechanics",
                    remote = true,
                    startingInputTypes = UserInput.class))
    public GameMechanics determineGameMechanics(UserInput userInput, GameRules gameRules, OperationContext context) {
        log.info("Determining mechanics from rules for: {}",
                gameRules.gameTitle());

        var prompt = promptResourceToString(mechanicsDeterminerPromptTemplate,
                Map.of("gameRules", gameRules.rulesText()));

        return context.ai().withDefaultLlm()
                .createObject(prompt, GameMechanics.class);
    }

    @Action
    @AchievesGoal(description = "Player count has been determined.",
            export = @Export(
                    name = "gamePlayerCount",
                    remote = true,
                    startingInputTypes = UserInput.class))
    public PlayerCount determinePlayerCount(UserInput userInput, GameRules gameRules, OperationContext context) {
        log.info("Determining player count from rules for: {}",
                gameRules.gameTitle());

        var prompt = promptResourceToString(playerCountPromptTemplate,
                Map.of("gameRules", gameRules.rulesText()));

        return context.ai().withDefaultLlm()
                .createObject(prompt, PlayerCount.class);
    }

    @Action
    public GameRules getGameRules(GameTitle gameTitle, RulesFile rulesFile) {
        log.info("Getting game rules for: " + gameTitle.gameTitle()
                + " from file: " + rulesFile.filename());

        if (rulesFile.successful()) {
            String rulesContent =
                    new TikaDocumentReader(new ClassPathResource(
                            rulesFilePath + "/" + rulesFile.filename()))
                            .get().stream().map(doc -> doc.getText())
                            .collect(Collectors.joining("\n"));

            return new GameRules(gameTitle.gameTitle(), rulesContent);

        }

        throw new RuntimeException(
                "Unable to fetch rules for the specified game.");
    }

    @Action
    public RulesFile getGameRulesFilename(GameTitle gameTitle, OperationContext context) {
        log.info("Getting game rules filename for: " + gameTitle.gameTitle());

        var prompt = promptResourceToString(rulesFetcherPromptTemplate,
                Map.of("gameTitle", gameTitle.gameTitle()));

        return context.ai().withDefaultLlm()
                .createObject(prompt, RulesFile.class);
    }

    private String promptResourceToString(
            Resource resource, Map<String, String> params) {
        try {
            var promptString =
                    resource.getContentAsString(Charset.defaultCharset());
            var stringTemplate = new ST(promptString, '{', '}');
            params.forEach(stringTemplate::add);
            return stringTemplate.render();
        } catch (IOException e) {
            log.error("Error reading prompt resource: " +
                    resource.getFilename(), e);
            return "";
        }
    }
}