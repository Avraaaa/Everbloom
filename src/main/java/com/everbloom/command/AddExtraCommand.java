package com.everbloom.command;

import com.everbloom.model.Extra;

import java.util.List;

public class AddExtraCommand implements Command {

    private final List<Extra> selectedExtras;
    private final Extra extra;

    public AddExtraCommand(List<Extra> selectedExtras, Extra extra) {
        this.selectedExtras = selectedExtras;
        this.extra = extra;
    }

    @Override
    public void execute() { selectedExtras.add(extra); }

    @Override
    public void undo() { selectedExtras.remove(extra); }
}
