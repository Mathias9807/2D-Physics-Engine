package physics;

import org.lwjgl.util.vector.Vector2f;

public class ShapeQuad extends Shape {

	public ShapeQuad(float xOrigin, float yOrigin, float xRadius, float yRadius, double mass) {
		super(xOrigin, yOrigin, mass);
		vertices 		= new Vector2f[4];
		vertices[0] 	= new Vector2f(xRadius, yRadius);
		vertices[1] 	= new Vector2f(xRadius, -yRadius);
		vertices[2] 	= new Vector2f(-xRadius, -yRadius);
		vertices[3] 	= new Vector2f(-xRadius, yRadius);
		double diagonal = Vector2f.sub(vertices[2], vertices[0], null).length();
		hitbox			= new Vector2f((float) diagonal * 0.5f, (float) diagonal * 0.5f);
	}

}
