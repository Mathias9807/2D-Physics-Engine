package physics;

import org.lwjgl.util.vector.*;

public class Shape {
	
	public Vector2f origin, lastPos, velocity, hitbox;
	public Vector2f[] vertices, normals;
	public double 	rotation, lastRot, angMom, mass, bounce;
	public boolean 	isRigid;
	
	public Shape(float xOrigin, float yOrigin, double mass, double rot) {
		origin 		= new Vector2f(xOrigin, yOrigin);
		lastPos		= new Vector2f(xOrigin, yOrigin);
		velocity	= new Vector2f();
		hitbox		= new Vector2f();
		rotation 	= rot;
		angMom		= 0;
		this.mass	= mass;
		bounce		= 1;
	}
	
	public Shape(float xOrigin, float yOrigin, double mass) {
		origin 		= new Vector2f(xOrigin, yOrigin);
		lastPos		= new Vector2f(xOrigin, yOrigin);
		velocity	= new Vector2f();
		hitbox		= new Vector2f();
		this.mass 	= mass;
		bounce		= 1;
	}
	
	public Shape setRigid(boolean b) {
		isRigid = b;
		return this;
	}

	public void tick(double delta) {
		lastPos.x = origin.x;
		lastPos.y = origin.y;
		lastRot   = rotation;
		if (isRigid) 
			return;
		origin.x += velocity.x * delta;
		origin.y += velocity.y * delta;
		rotation += angMom * delta;
		velocity.x -= velocity.x * World.DAMP * delta;
		velocity.y -= velocity.y * World.DAMP * delta;
		angMom -= angMom * World.ROT_DAMP * delta;
	}
	
	public boolean collides(Shape s) {
		return false;
	}
	
	public void react(Shape s) {
	}
	
	public void addForce(Vector2f f, Vector2f p) {
		if (p.x == 0 && p.y == 0) {
			velocity.x += f.x / mass;
			velocity.y += f.y / mass;
			return;
		}
		Vector2f d = new Vector2f(p);
		d.normalise();
		float dot = Math.abs(Vector2f.dot(f, d));
		velocity.x += f.x * dot / mass;
		velocity.y += f.y * dot / mass;
		
		float t = crossZ(f, d);
		angMom -= t / Math.PI * 180 / mass;
	}
	
	public void stepBack() {
		Vector2f temp = new Vector2f(origin);
		origin = lastPos;
		lastPos = temp;
		double tempRot = rotation;
		rotation = lastRot;
		lastRot = tempRot;
	}
	
	public void checkNormals() {
		if (normals == null) {
			normals = new Vector2f[vertices.length];
			for (int i = 0; i < vertices.length; i++) {
				Vector2f edge = new Vector2f(0, 0);
				edge.x = vertices[(i + 1) % vertices.length].x - vertices[i].x;
				edge.y = vertices[(i + 1) % vertices.length].y - vertices[i].y;
				normals[i] = getEdgeNormal(edge);
			}
		}
	}
	
	public Vector2f getVertexInWorld(int i) {
		Vector2f p = vertices[i];
		Vector2f r = new Vector2f(0, 0);
		float angleR = (float) (rotation / 180 * Math.PI);
		r.x = (float) (origin.x + p.x * Math.cos(angleR) - p.y * Math.sin(angleR));
		r.y = (float) (origin.y + p.y * Math.cos(angleR) + p.x * Math.sin(angleR));
		return r;
	}
	
	public Vector2f[] getVerticesInWorld() {
		Vector2f[] r = new Vector2f[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			r[i] = getVertexInWorld(i);
		}
		return r;
	}
	
	public Vector2f getVertex(int i) {
		Vector2f p = getVertexInWorld(i);
		return new Vector2f(p.x - origin.x, p.y - origin.y);
	}
	
	public Vector2f[] getVertices() {
		Vector2f[] r = new Vector2f[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			r[i] = getVertex(i);
		}
		return r;
	}
	
	public Vector2f getNormalInWorld(int i) {
		Vector2f n = normals[i];
		Vector2f r = new Vector2f();
		r.x = (float) (n.x * Math.cos(rotation) - n.y * Math.sin(rotation));
		r.y = (float) (n.y * Math.cos(rotation) + n.x * Math.sin(rotation));
		return r;
	}
	
	private float crossZ(Vector2f v0, Vector2f v1) {
		return v0.x * v1.y - v0.y * v1.x;
	}
	
	public Vector2f getEdgeNormal(Vector2f v) {
		Vector3f r = Vector3f.cross(new Vector3f(v.x, v.y, 0), new Vector3f(0, 0, 1), null);
		return new Vector2f(r.x, r.y);
	}

}
