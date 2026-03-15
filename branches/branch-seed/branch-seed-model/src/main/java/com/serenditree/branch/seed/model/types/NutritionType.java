package com.serenditree.branch.seed.model.types;

public enum NutritionType {
    WATER,
    NUBITS;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
