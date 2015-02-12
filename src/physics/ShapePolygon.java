package physics;

import org.lwjgl.util.vector.Vector2f;

public class ShapePolygon extends Shape {

	public ShapePolygon(float xOrigin, float yOrigin, double mass, Vector2f[] v) {
		super(xOrigin, yOrigin, mass);
		vertices = v;
		
		float farthest = v[0].lengthSquared();
		for (Vector2f p : v) 
			if (farthest < p.lengthSquared()) farthest = p.lengthSquared();
		farthest = (float) Math.sqrt(farthest);
		hitbox.x = farthest;
		hitbox.y = farthest;
	}

}
