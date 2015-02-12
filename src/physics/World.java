package physics;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.vector.Vector2f.*;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import dev.Main;

public class World {
	
	public static final double 	GRAVITY_PULL 	= 9.8;
	public static final double 	DAMP 			= 0.1f;
	public static final double 	ROT_DAMP 		= 0.1f;
	
	private static boolean 		useGravity 		= false;
	
	public static boolean 		collision 		= false;
	
	public static List<Shape> 	bodies;
	
	public static void start(boolean gravity) {
		useGravity = gravity;
		bodies = new ArrayList<Shape>();
	}
	
	public static void tick(double delta) {
		Main.running = true;
		for (int i = 0; i < bodies.size(); i++) {
			Shape s = bodies.get(i);
			
			if (useGravity) s.addForce(new Vector2f(0, (float) (-GRAVITY_PULL * delta * s.mass)), 
					new Vector2f(0, 0));
			
			Vector3f normal = new Vector3f();
			for (int j = 0; j < bodies.size(); j++) {
				if (s.equals(bodies.get(j))) continue;
				if (checkCollision(s, bodies.get(j))) 
					if (SATCheck(s, bodies.get(j), normal).collides) 
						react(s, bodies.get(j), normal);
			}
			
			s.tick(delta);
		}
	}
	
	private static void react(Shape s0, Shape s1, Vector3f normal) {
		collision = true;
		
		Vector2f force = sub(s0.origin, s1.origin, null);
		s0.addForce(force, new Vector2f(0, 0));
		s1.addForce(force.negate(null), new Vector2f(0, 0));
		
		/*s0.velocity.y = -(s0.velocity.y - s1.velocity.y);
		s1.velocity.y = -(s1.velocity.y - s0.velocity.y);
		s0.stepBack();
		s1.stepBack();*/
		
		/*s0.origin.set(s0.lastPos);
		s1.origin.set(s1.lastPos);
		Vector2f vel0 = new Vector2f(s0.velocity.x, s0.velocity.y);
		Vector2f vel1 = new Vector2f(s1.velocity.x, s1.velocity.y);
		Vector2f newVel0 = new Vector2f(vel0.x - vel1.x, vel0.y - vel1.y);
		Vector2f newVel1 = new Vector2f(vel1.x - vel0.x, vel1.y - vel0.y);
		s0.velocity = (Vector2f) newVel0.scale((float) s0.bounce);
		s1.velocity = (Vector2f) newVel1.scale((float) s1.bounce);
		System.out.println(newVel0);
		System.out.println(newVel1);
		Main.running = false;*/
	}

	private static boolean checkCollision(Shape s0, Shape s1) {
		if (s0.origin.x + s0.hitbox.x < s1.origin.x - s1.hitbox.x) return false;
		if (s0.origin.x - s0.hitbox.x > s1.origin.x + s1.hitbox.x) return false;
		if (s0.origin.y + s0.hitbox.y < s1.origin.y - s1.hitbox.y) return false;
		if (s0.origin.y - s0.hitbox.y > s1.origin.y + s1.hitbox.y) return false;
		return true;
	}
	
	private static CData SATCheck(Shape s0, Shape s1, Vector3f normal) {
		// Prepare collision data. 
		CData c = new CData();
		
		// All the vertices of both shapes in world space.
		final Vector2f[] a0 = s0.getVerticesInWorld();
		final Vector2f[] a1 = s1.getVerticesInWorld();
		
		// Quit-early test. 
		Vector2f earlyN = new Vector2f(s0.origin.x - s1.origin.x, s0.origin.y - s1.origin.y);
		if (earlyN.x != 0 || earlyN.y != 0) earlyN.normalise();
		if (!SATSingleCheck(a0, a1, earlyN).collides) {
			c.collides = false;
			return c;
		}
		
		// Set true as the default value. 
		c.collides = true;
		
		// Check the first shape. 
		for (int i = 0; i < a0.length; i++) {
			CData data = SATSingleCheck(a0, a1, s0.getNormalInWorld(i));
			if (!data.collides) {
				c.collides = false;
			}else {
				c.penetration = Math.min(data.penetration, c.penetration);
			}
		}
		
		// Check the second shape. 
		for (int i = 0; i < a1.length; i++) {
			CData data = SATSingleCheck(a0, a1, s1.getNormalInWorld(i));
			if (!data.collides) {
				c.collides = false;
			}else {
				c.penetration = Math.min(data.penetration, c.penetration);
			}
		}
		
		System.out.println(c.penetration);
		
		return c;
	}
	
	/**
	 * Returns a CData object with <code>collision</code> set to true if collision. 
	 * @param a0
	 * @param a1
	 * @param normal
	 * @return
	 */
	private static CData SATSingleCheck(Vector2f[] a0, Vector2f[] a1, Vector2f normal) {
		// Prepare Collision data object. 
		CData c = new CData();
		c.normal = new Vector2f(normal);
		
		// Creates two arrays with the vertices of each shapes projected unto the normal. 
		float[] p0 = new float[a0.length], p1 = new float[a1.length];
		for (int i = 0; i < a0.length; i++) {
			p0[i] = Vector2f.dot(a0[i], normal);
		}
		for (int i = 0; i < a1.length; i++) {
			p1[i] = Vector2f.dot(a1[i], normal);
		}
		
		// Finds the highest and lowest vertices on each shape. 
		float 	high0 = p0[0], low0 = p0[1], 
				high1 = p1[0], low1 = p1[1];
		for (float f : p0) {
			if (f > high0) 	high0 = f;
			if (f < low0) 	low0  = f;
		}
		for (float f : p1) {
			if (f > high1) 	high1 = f;
			if (f < low1) 	low1  = f;
		}
		
		// Compares the highest and lowest vertices of the shapes to find an overlap. 
		if (low0 < high1 && low0 > low1) {
			c.collides = true;
			c.penetration = high1 - low0;
			return c;
		}
		if (high0 < high1 && high0 > low1) {
			c.collides = true;
			c.penetration = high0 - low1;
			return c;
		}
		if (low1 < high0 && low1 > low0) {
			c.collides = true;
			c.penetration = high0 - low1;
			return c;
		}
		if (high1 < high0 && high1 > low0) {
			c.collides = true;
			c.penetration = high1 - low0;
			return c;
		}
		c.collides = false;
		
		return c;
	}
	
	public static void addBody(Shape s) {
		bodies.add(s);
	}
	
}
