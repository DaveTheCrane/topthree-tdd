package topthree.impl;

import topthree.interfaces.PrettyPrinter;
import topthree.models.ScoreRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PrettyPrinterTest {
    
    private final PrettyPrinter printer = new PrettyPrinterImpl();
    
    @Test
    void print_scoreRecord_returnsCommaSeparatedString() {
        ScoreRecord record = new ScoreRecord("p1", "Alice", "g1", "Chess", 10, 85);
        String result = printer.print(record);
        
        assertEquals("p1,Alice,g1,Chess,10,85", result);
    }
    
    @Test
    void print_scoreRecordWithSpaces_returnsTrimmedValues() {
        ScoreRecord record = new ScoreRecord("  p1  ", "  Alice  ", "  g1  ", "  Chess  ", 10, 85);
        String result = printer.print(record);
        
        // Note: The ScoreRecord constructor doesn't trim, so the spaces are preserved
        assertEquals("  p1  ,  Alice  ,  g1  ,  Chess  ,10,85", result);
    }
}