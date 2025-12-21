package com.jmdecerio.spring_ai_embabel.model;

import org.checkerframework.checker.units.qual.A;

public record GameMechanics(String gameTitle, String mechanics) implements Answer {
}
