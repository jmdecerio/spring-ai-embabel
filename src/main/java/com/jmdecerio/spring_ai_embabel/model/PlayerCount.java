package com.jmdecerio.spring_ai_embabel.model;

import org.checkerframework.checker.units.qual.A;

public record PlayerCount(String gameTitle, int minPlayers, int maxPlayers) implements Answer {
}
