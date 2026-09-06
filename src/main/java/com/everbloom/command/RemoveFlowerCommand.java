package com.everbloom.command;

import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetFlower;

public class RemoveFlowerCommand implements Command {

    private final BouquetBuilder bouquetBuilder;
    private final BouquetFlower bouquetFlower;

    public RemoveFlowerCommand(BouquetBuilder bouquetBuilder, BouquetFlower bouquetFlower) {
        this.bouquetBuilder = bouquetBuilder;
        this.bouquetFlower = bouquetFlower;
    }

    @Override
    public void execute() { bouquetBuilder.removeFlower(bouquetFlower.getFlower()); }

    @Override
    public void undo() { bouquetBuilder.addFlower(bouquetFlower.getFlower(), bouquetFlower.getQuantity()); }
}
