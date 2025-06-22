package priv.wzw.onw;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testHashSet() {
        HashSet<Player> hashSet = new HashSet<>();
        Player p1 = new Player();
        p1.setUserId("1");
        p1.setRoomId("a");

        Player p2 = new Player();
        p2.setUserId("1");
        p2.setRoomId("b");
        hashSet.add(p1);
        hashSet.add(p2);
        assertEquals(1, hashSet.size());
        System.out.println(hashSet);
    }
}
