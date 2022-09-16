package org.breskul.actions;

public interface Action {
    void execute();

    ActionPriority priority();
}