import com.wordle.describe
import com.wordle.invert
import com.wordle.toLetterSet
import org.junit.Assert.assertEquals
import org.junit.Test

class Tests {

    @Test
    fun `letter a`() {
        assertEquals(1, "a".toLetterSet())
        assertEquals(1, "A".toLetterSet())
        assertEquals(1, "aa".toLetterSet())
    }

    @Test
    fun `letter b`() {
        assertEquals(2, "b".toLetterSet())
        assertEquals(2, "B".toLetterSet())
        assertEquals(2, "bb".toLetterSet())
    }

    @Test
    fun `letter c`() {
        assertEquals(4, "c".toLetterSet())
        assertEquals(4, "C".toLetterSet())
        assertEquals(4, "cc".toLetterSet())
    }

    @Test
    fun `letter z`() {
        assertEquals(33554432, "z".toLetterSet())
        assertEquals(33554432, "Z".toLetterSet())
        assertEquals(1 shl 25, "zz".toLetterSet())
    }

    @Test
    fun `a b c`() {
        assertEquals(0b111, "abc".toLetterSet())
    }

    @Test
    fun `word alan`() {
        assertEquals(0b10100000000001, "alan".toLetterSet())
        //             nmlkjihgfedcba
    }

    @Test
    fun `invert 1`() {
        assertEquals(0b11111111111101011111111110, "alan".toLetterSet().invert())
        //             zyxwvutsrqponmlkjihgfedcba
    }

    @Test
    fun `describe set for word alan`() {
        assertEquals("aln", "alan".toLetterSet().describe())
    }
}
