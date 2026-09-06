package com.everbloom.command;

import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.Flower;

public class AddFlowerCommand implements Command {

    private final BouquetBuilder bouquetBuilder;
    private final Flower flower;
    private final int quantity;

    public AddFlowerCommand(BouquetBuilder bouquetBuilder, Flower flower, int quantity) {
        this.bouquetBuilder = bouquetBuilder;
        this.flower = flower;
        this.quantity = quantity;
    }

    @Override
    public void execute() { bouquetBuilder.addFlower(flower, quantity); }

    @Override
    public void undo() { bouquetBuilder.removeFlowerQuantity(flower, quantity); }
}
