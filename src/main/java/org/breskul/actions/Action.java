package org.breskul.actions;

public interface Action {
    public void execute();

    public ActionPriority getActionPriority();
}