package org.breskul.actions;

public interface Action {
    public void execute(boolean showSql);

    public ActionPriority getActionPriority();
}