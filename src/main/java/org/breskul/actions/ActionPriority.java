package org.breskul.actions;

public enum ActionPriority {

    DELETE(3), INSERT(2), UPDATE(1);

    public int priority;
    ActionPriority(int priority) {
        this.priority = priority;
    }

}
