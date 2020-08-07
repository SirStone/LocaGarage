package bots.sparsity;

import static org.junit.Assert.*;

import org.junit.Test;

import bots.basic.F;
import bots.basic.Statics;

public class SparseBitMatrix3DTest {

	@Test
	public void test() {
		SparseBitMatrix3D sbm3d = new SparseBitMatrix3D(10, 5, 3);
		sbm3d.set(5,3,2);
		int key = Statics.cantor(5, 3, 2);
		int[] unk = Statics.cantorInverse3values(key);
		F.p(sbm3d,unk);
		
		assertTrue(sbm3d.get(5, 3, 2));
	}

}
