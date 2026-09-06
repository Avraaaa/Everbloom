package com.everbloom.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandHistory {

    private final Deque<Command> undoCommands = new ArrayDeque<>();
    private final Deque<Command> redoCommands = new ArrayDeque<>();

    public void execute(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command is required.");
        }
        command.execute();
        undoCommands.push(command);
        redoCommands.clear();
    }

    public boolean undo() {
        if (undoCommands.isEmpty()) {
            return false;
        }
        Command command = undoCommands.pop();
        command.undo();
        redoCommands.push(command);
        return true;
    }

    public boolean redo() {
        if (redoCommands.isEmpty()) {
            return false;
        }
        Command command = redoCommands.pop();
        command.execute();
        undoCommands.push(command);
        return true;
    }

    public boolean canUndo() { return !undoCommands.isEmpty(); }
    public boolean canRedo() { return !redoCommands.isEmpty(); }
    public void clear() { undoCommands.clear(); redoCommands.clear(); }
}
