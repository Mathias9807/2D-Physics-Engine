package dev;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;

import physics.*;

public class Main {
	
	public static final int SAMPLES = 8;
	
	public static double time;
	public static double ratio;
	
	public static boolean running	= true;
	
	public Main() {
		ratio = (double) Display.getWidth() / Display.getHeight();
		
		glMatrixMode(GL_PROJECTION);
		int worldWidth = 10;
		glOrtho(-worldWidth, worldWidth, -worldWidth / ratio, worldWidth / ratio, -1, 1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		World.start(true);
		Shape s0 		= new ShapeQuad(-4, 1f, 0.5f, 0.5f, 1);
//		s0.velocity.x 	= 2f;
//		s0.velocity.y 	= 10f;
//		s0.angMom		= 300f;
		Shape s1 		= new ShapeQuad(5, 0, 0.5f, 0.5f, 1);
//		s1.velocity.x 	= -8f;
//		s1.velocity.y 	= 5f;
//		s1.angMom		= -200f;
		
		World.addBody(s0);
		World.addBody(s1);
		
		loop();
	}
	
	private void tick(double delta) {
		for (int i = 0; i < World.bodies.size(); i++) {
			Shape s = World.bodies.get(i);
			Vector2f vec = s.getVertex(0);
			if (s.getVertexInWorld(0).length() > 2) {
				s.addForce(new Vector2f((float) (-s.getVertexInWorld(0).x * 15 * delta), 
						(float) (-s.getVertexInWorld(0).y * 15 * delta)), 
						new Vector2f(vec.x, vec.y));
			}
		}
		
		/*if (Mouse.isButtonDown(0)) {
			Shape s = World.bodies.get((int) (Math.random() * World.bodies.size()));
			s.addForce(new Vector2f(Mouse.getDX() / 10f, Mouse.getDY() / 10f), 
						s.getVertex(0));
		}*/
		
		World.tick(delta);
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		if (World.collision) glClearColor(1, 1, 1, 0);
		else glClearColor(0.8f, 0.8f, 0.8f, 0);
		World.collision = false;
		glLoadIdentity();
		
		for (int i = 0; i < World.bodies.size(); i++) {
			Shape s = World.bodies.get(i);
			glColor3d(i % 3 == 0 ? 1 : 0, (i + 1) % 3 == 0 ? 1 : 0, (i + 2) % 3 == 0 ? 1 : 0);
			glBegin(GL_LINE_STRIP);
			{
				for (int j = 0; j < s.vertices.length + 1; j++) {
					Vector2f v = s.getVertexInWorld(j % s.vertices.length);
					glVertex2d(v.x, v.y);
				}
			}
			glEnd();
		}
		
		glColor3d(0, 0, 0);
		glBegin(GL_LINES);
		{
			for (int i = 0; i < World.bodies.size(); i++) {
				glVertex2d(0, 0);
				glVertex2d(World.bodies.get(i).getVertexInWorld(0).x, 
						World.bodies.get(i).getVertexInWorld(0).y);
			}
		}
		glEnd();
	}
	
	private void loop() {
		long lastTime = System.nanoTime();
		double delta = 0;
		double ns = 1000000000.0 / 60.0;
		while (!Display.isCloseRequested()) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1) {
				tick((1d / 60) * (true ? 1 : 0.125));
				time++;
				delta--;
			}
			render();
			Display.update();
			
			int error = glGetError();
			if (error != GL_NO_ERROR) Display.setTitle(GLU.gluErrorString(error));
			
		}
		try {
			Display.releaseContext();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		Display.destroy();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		try {
			Display.setDisplayMode(Display.getAvailableDisplayModes()[1]);
			Display.setFullscreen(false);
			Display.setTitle("Physics");
			Display.setVSyncEnabled(true);
			Display.create(new PixelFormat(0, 0, 0, SAMPLES));
			Mouse.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		new Main();
	}
	
}
