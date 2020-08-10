package bots.basic;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class StaticsTest {

	@Test
	public void cantorTest() {
		Random random = new Random();
		
		int a = random.nextInt(1000);
		int b = random.nextInt(1000);
		
		int z = Statics.cantor(a, b);
		int[] ab = Statics.cantorInverse2values(z);
		assertTrue(a==ab[0] && b==ab[1]);
		F.p(a,b,z,ab[0],ab[1]);
	}

	@Test
	public void numberToRomanTest() {
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			int random_int = random.nextInt(1000);
			F.p(random_int, "->", Statics.toRoman(random_int));
		}
	}
}
