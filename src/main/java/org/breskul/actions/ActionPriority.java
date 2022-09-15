package org.breskul.actions;

public enum ActionPriority {
    INSERT_PRIORITY(1),
    UPDATE_PRIORITY(2),
    DELETE_PRIORITY(3);

    private final int priority;

    ActionPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
