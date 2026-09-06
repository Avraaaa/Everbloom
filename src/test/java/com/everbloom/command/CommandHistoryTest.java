package com.everbloom.command;

import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.Extra;
import com.everbloom.model.Flower;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHistoryTest {

    @Test
    void executesAndUndoesFlowerAddition() {
        BouquetBuilder builder = new BouquetBuilder();
        CommandHistory history = new CommandHistory();
        Flower rose = flower(1, "Rose");

        history.execute(new AddFlowerCommand(builder, rose, 3));

        assertEquals(3, builder.getSelectedFlowers().getFirst().getQuantity());
        assertTrue(history.canUndo());
        history.undo();
        assertTrue(builder.getSelectedFlowers().isEmpty());
        assertTrue(history.canRedo());
    }

    @Test
    void executesAndUndoesFlowerRemoval() {
        BouquetBuilder builder = new BouquetBuilder();
        Flower rose = flower(1, "Rose");
        builder.addFlower(rose, 4);
        CommandHistory history = new CommandHistory();

        history.execute(new RemoveFlowerCommand(builder, builder.getSelectedFlowers().getFirst()));

        assertTrue(builder.getSelectedFlowers().isEmpty());
        history.undo();
        assertEquals(4, builder.getSelectedFlowers().getFirst().getQuantity());
    }

    @Test
    void executesAndUndoesExtraAdditionAndRemoval() {
        List<Extra> extras = new ArrayList<>();
        Extra card = new Extra(1, "Greeting Card", "Card", 1200, true);
        CommandHistory history = new CommandHistory();

        history.execute(new AddExtraCommand(extras, card));
        assertEquals(List.of(card), extras);
        history.undo();
        assertTrue(extras.isEmpty());

        extras.add(card);
        history.execute(new RemoveExtraCommand(extras, card));
        assertTrue(extras.isEmpty());
        history.undo();
        assertEquals(List.of(card), extras);
    }

    @Test
    void maintainsMultipleSequentialCommandsAndRedo() {
        BouquetBuilder builder = new BouquetBuilder();
        CommandHistory history = new CommandHistory();
        Flower rose = flower(1, "Rose");
        Flower lily = flower(2, "Lily");

        history.execute(new AddFlowerCommand(builder, rose, 2));
        history.execute(new AddFlowerCommand(builder, lily, 1));
        history.undo();
        history.undo();
        assertTrue(builder.getSelectedFlowers().isEmpty());

        history.redo();
        assertEquals("Rose", builder.getSelectedFlowers().getFirst().getFlower().getName());
        assertEquals(2, builder.getSelectedFlowers().getFirst().getQuantity());
        history.redo();
        assertEquals(2, builder.getSelectedFlowers().size());
    }

    @Test
    void clearsRedoHistoryAfterNewCommand() {
        BouquetBuilder builder = new BouquetBuilder();
        CommandHistory history = new CommandHistory();
        Flower rose = flower(1, "Rose");
        Flower lily = flower(2, "Lily");

        history.execute(new AddFlowerCommand(builder, rose, 1));
        history.undo();
        assertTrue(history.canRedo());
        history.execute(new AddFlowerCommand(builder, lily, 1));

        assertFalse(history.canRedo());
        assertEquals("Lily", builder.getSelectedFlowers().getFirst().getFlower().getName());
    }

    private Flower flower(long id, String name) {
        return new Flower(id, name, "Color", 3000, 10, true);
    }
}
