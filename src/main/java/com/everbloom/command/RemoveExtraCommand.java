package com.everbloom.command;

import com.everbloom.model.Extra;

import java.util.List;

public class RemoveExtraCommand implements Command {

    private final List<Extra> selectedExtras;
    private final Extra extra;
    private int index;

    public RemoveExtraCommand(List<Extra> selectedExtras, Extra extra) {
        this.selectedExtras = selectedExtras;
        this.extra = extra;
    }

    @Override
    public void execute() {
        index = selectedExtras.indexOf(extra);
        selectedExtras.remove(extra);
    }

    @Override
    public void undo() {
        selectedExtras.add(index, extra);
    }
}
