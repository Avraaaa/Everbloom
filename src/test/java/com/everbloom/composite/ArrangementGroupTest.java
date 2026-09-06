package com.everbloom.composite;
import com.everbloom.model.BouquetItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ArrangementGroupTest {
    @Test void calculatesNestedPackageTotalAndSummary() {
        ArrangementGroup wedding = new ArrangementGroup("Wedding Package");
        ArrangementGroup ceremony = new ArrangementGroup("Ceremony");
        ceremony.add(new BouquetArrangement("Bridal Bouquet", item(12000)));
        ceremony.add(new BouquetArrangement("Altar Arrangement", item(18000)));
        ArrangementGroup reception = new ArrangementGroup("Reception");
        reception.add(new BouquetArrangement("Head Table", item(9000)));
        wedding.add(ceremony); wedding.add(reception);
        assertEquals(39000, wedding.getTotalPrice());
        assertTrue(wedding.getSummary().contains("Ceremony"));
        assertTrue(wedding.getSummary().contains("Bridal Bouquet: BDT 12000"));
    }
    private BouquetItem item(long price) { return new BouquetItem() { public String getDescription() { return "Bouquet"; } public long getSubtotal() { return price; } }; }
}
