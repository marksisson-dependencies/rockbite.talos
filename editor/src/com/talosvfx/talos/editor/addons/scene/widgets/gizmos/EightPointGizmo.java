package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.talosvfx.talos.editor.addons.scene.widgets.gizmos.EightPointGizmo.ControlPointType.CORNER;
import static com.talosvfx.talos.editor.addons.scene.widgets.gizmos.EightPointGizmo.ControlPointType.MIDDLE;

public class EightPointGizmo extends Gizmo {

	private static final Logger logger = LoggerFactory.getLogger(SpriteTransformGizmo.class);


	public enum ControlPointType {
		CORNER,
		MIDDLE
	}

	private static class ControlPoint {
		private Vector2 position = new Vector2();
		private ControlPointType pointType;

		private int id;

		public ControlPoint (int id, ControlPointType type) {
			this.id = id;
			this.pointType = type;
		}
	}

	private Array<ControlPoint> controlPoints = new Array<>();

	private static final int BOTTOM_LEFT = 0;
	private static final int LEFT_MIDDLE = 1;
	private static final int TOP_LEFT = 2;
	private static final int TOP_MIDDLE = 3;
	private static final int TOP_RIGHT = 4;
	private static final int RIGHT_MIDDLE = 5;
	private static final int BOTTOM_RIGHT = 6;
	private static final int BOTTOM_MIDDLE = 7;

	private final TextureRegion whitePixelRegion;
	private final Image controlRect;

	private boolean userInteracted = false;

	public EightPointGizmo () {
		for (int i = 0; i < 8; i++) {
			controlPoints.add(new ControlPoint(-1, CORNER));//dummy
		}
		register(CORNER, BOTTOM_LEFT);
		register(MIDDLE, LEFT_MIDDLE);
		register(CORNER, TOP_LEFT);
		register(MIDDLE, TOP_MIDDLE);
		register(CORNER, TOP_RIGHT);
		register(MIDDLE, RIGHT_MIDDLE);
		register(CORNER, BOTTOM_RIGHT);
		register(MIDDLE, BOTTOM_MIDDLE);

		whitePixelRegion = SharedResources.skin.getRegion("white-pixel");
		Drawable rectDrawable = SharedResources.skin.newDrawable("white-pixel");
		controlRect = new Image(rectDrawable);
	}

	private void register (ControlPointType type, int id) {
		controlPoints.set(id, new ControlPoint(id, type));
	}

	@Override
	public void act (float delta) {
		super.act(delta);

		if (!userInteracted) { //Truth is the component
			SpriteRendererComponent spriteRenderComponent = gameObject.getComponent(SpriteRendererComponent.class);
			TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
			componentToPoints(transformComponent, spriteRenderComponent);
		}
	}

	private void componentToPoints (TransformComponent transformComponent, SpriteRendererComponent spriteRenderComponent) {
		Vector2 worldPosition = transformComponent.worldPosition;
		float worldRotation = transformComponent.worldRotation;
		Vector2 worldScale = transformComponent.worldScale;

		Vector2 size = spriteRenderComponent.size;

		float calculatedWidth = worldScale.x * size.x;
		float calculatedHeight = worldScale.y * size.y;

		float halfWidth = calculatedWidth / 2f;
		float halfHeight = calculatedHeight / 2f;

		controlPoints.get(BOTTOM_LEFT).position.set(-halfWidth, -halfHeight).rotateDeg(worldRotation).add(worldPosition);
		controlPoints.get(LEFT_MIDDLE).position.set(-halfWidth, 0).rotateDeg(worldRotation).add(worldPosition);
		controlPoints.get(TOP_LEFT).position.set(-halfWidth, halfHeight).rotateDeg(worldRotation).add(worldPosition);

		controlPoints.get(TOP_MIDDLE).position.set(0, halfHeight).rotateDeg(worldRotation).add(worldPosition);
		controlPoints.get(BOTTOM_MIDDLE).position.set(0, -halfHeight).rotateDeg(worldRotation).add(worldPosition);

		controlPoints.get(TOP_RIGHT).position.set(halfWidth, halfHeight).rotateDeg(worldRotation).add(worldPosition);
		controlPoints.get(RIGHT_MIDDLE).position.set(halfWidth, 0).rotateDeg(worldRotation).add(worldPosition);
		controlPoints.get(BOTTOM_RIGHT).position.set(halfWidth, -halfHeight).rotateDeg(worldRotation).add(worldPosition);

	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (isSelected()) {

			SpriteRendererComponent spriteRenderComponent = gameObject.getComponent(SpriteRendererComponent.class);
			TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

			drawPixelLines(batch, spriteRenderComponent, transformComponent);

			for (ControlPoint controlPoint : controlPoints) {
				float size = 8 * worldPerPixel;
				controlRect.setSize(size, size);

				controlRect.setPosition(controlPoint.position.x - size / 2f, controlPoint.position.y - size / 2f);
				controlRect.draw(batch, parentAlpha);
			}

//			batch.end();
//			ShapeRenderer shapeRenderer = new ShapeRenderer();
//			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
//			shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
//
//			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//			shapeRenderer.setColor(Color.RED);
//			shapeRenderer.rect(boundingBox.min.x, boundingBox.min.y, boundingBox.getWidth(), boundingBox.getHeight());
//			shapeRenderer.end();
//
//			shapeRenderer.dispose();
//			batch.begin();
		}

	}

	private void drawPixelLines (Batch batch, SpriteRendererComponent spriteRenderComponent, TransformComponent transformComponent) {
		Vector2 worldPosition = transformComponent.worldPosition;
		float worldRotation = transformComponent.worldRotation;
		Vector2 worldScale = transformComponent.worldScale;

		Vector2 size = spriteRenderComponent.size;

		float calculatedWidth = worldScale.x * size.x;
		float calculatedHeight = worldScale.y * size.y;

		float halfWidth = calculatedWidth / 2f;
		float halfHeight = calculatedHeight / 2f;

		float pixelSize = worldPerPixel;

		batch.setColor(Color.valueOf("387ede"));

		drawPixelLine(batch, BOTTOM_LEFT, TOP_LEFT, pixelSize, true);
		drawPixelLine(batch, TOP_LEFT, TOP_RIGHT, pixelSize, false);
		drawPixelLine(batch, BOTTOM_RIGHT, TOP_RIGHT,pixelSize, true);
		drawPixelLine(batch, BOTTOM_LEFT, BOTTOM_RIGHT, pixelSize, false);

		batch.setColor(Color.WHITE);

	}

	Vector2 temp3 = new Vector2();
	private void drawPixelLine (Batch batch, int from, int to, float pixelSize, boolean vertical) {
		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

		ControlPoint fromControlPoint = controlPoints.get(from);
		ControlPoint toControlPoint = controlPoints.get(to);

		float midPointX = (fromControlPoint.position.x + toControlPoint.position.x) / 2f;
		float midPointY = (fromControlPoint.position.y + toControlPoint.position.y) / 2f;

		temp3.set(fromControlPoint.position).sub(toControlPoint.position);
		float length = temp3.len();

		if (vertical) {
			batch.draw(whitePixelRegion, midPointX - pixelSize/2f, midPointY - length/2f, pixelSize/2f, length/2f, pixelSize, length, 1f, 1f, transformComponent.worldRotation);
		} else {
			batch.draw(whitePixelRegion, midPointX - length/2f, midPointY - pixelSize/2f, length/2f, pixelSize/2f, length, pixelSize, 1f, 1f, transformComponent.worldRotation);
		}


	}

	private ControlPoint currentManipulatingPoint = null;
	private Vector2 controlPointOffset = new Vector2();

	@Override
	public void touchDown (float x, float y, int button) {
		userInteracted = true;
		super.touchDown(x, y, button);

		ControlPoint touchedPoint = getTouchedPoint(x, y);
		if (touchedPoint != null) {
			//Lets store the relative position for start

			currentManipulatingPoint = touchedPoint;

			controlPointOffset.set(x, y).sub(touchedPoint.position);

		}
	}

	@Override
	public void touchDragged (float x, float y) {
		userInteracted = true;
		super.touchDragged(x, y);

		float deltaX = x - controlPointOffset.x;
		float deltaY = y - controlPointOffset.y;

		if (currentManipulatingPoint != null) {
			movePointByDelta(x, y, deltaX, deltaY, currentManipulatingPoint);
		}
	}

	private void movePointByDelta (float x, float y, float deltaX, float deltaY, ControlPoint currentManipulatingPoint) {

		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

		switch (currentManipulatingPoint.pointType) {
		case CORNER:

			currentManipulatingPoint.position.set(x, y).sub(controlPointOffset);

			//We move all other points apart from the opposite point which is always 4 away

			//If its a corner, we moving a bunch of these points

			int id = currentManipulatingPoint.id;

			int ignore = id + 4;
			ignore %= 8;
			if (ignore < 0) ignore += 8;

			//Lets update the 2 other corners, -2 + 2

			ControlPoint stationaryPoint = controlPoints.get(ignore);
			BoundingBox rect = getUnrotateRectFromOppositeCorners(currentManipulatingPoint, stationaryPoint);

			//Unrotate all points


			for (ControlPoint controlPoint : controlPoints) {
				controlPoint.position.sub(transformComponent.worldPosition);
				controlPoint.position.rotateDeg(-transformComponent.worldRotation);
			}

			float width = rect.getWidth();
			float height = rect.getHeight();

			//need to determine if our width/height is negative too

			//We can do this because we are unrotated rect atm



			//brain cant work it the algorithm, gonna do monkey way

			if (stationaryPoint.id == TOP_LEFT) {
				//Do the other corners


				//Have to compare it to things we are taking truth from
				boolean invertedX = controlPoints.get(TOP_LEFT).position.x > controlPoints.get(BOTTOM_RIGHT).position.x;
				boolean invertedY = controlPoints.get(TOP_LEFT).position.y < controlPoints.get(BOTTOM_RIGHT).position.y;

				if (invertedX) {
					width *= -1;
				}
				if (invertedY) {
					height *= -1;
				}


				controlPoints.get(TOP_RIGHT).position.set(stationaryPoint.position.x + width, stationaryPoint.position.y);
				controlPoints.get(BOTTOM_LEFT).position.set(stationaryPoint.position.x, stationaryPoint.position.y - height);
				controlPoints.get(TOP_MIDDLE).position.set(stationaryPoint.position.x + width/2f, stationaryPoint.position.y);
				controlPoints.get(RIGHT_MIDDLE).position.set(stationaryPoint.position.x + width, stationaryPoint.position.y - height/2f);
				controlPoints.get(LEFT_MIDDLE).position.set(stationaryPoint.position.x, stationaryPoint.position.y - height/2f);
				controlPoints.get(BOTTOM_MIDDLE).position.set(stationaryPoint.position.x + width/2f, stationaryPoint.position.y - height);
			}

			if (stationaryPoint.id == BOTTOM_RIGHT) {
				//Do the other corners

				//Have to compare it to things we are taking truth from
				boolean invertedX = controlPoints.get(TOP_LEFT).position.x > controlPoints.get(BOTTOM_RIGHT).position.x;
				boolean invertedY = controlPoints.get(TOP_LEFT).position.y < controlPoints.get(BOTTOM_RIGHT).position.y;

				if (invertedX) {
					width *= -1;
				}
				if (invertedY) {
					height *= -1;
				}


				controlPoints.get(TOP_RIGHT).position.set(stationaryPoint.position.x, stationaryPoint.position.y + height);
				controlPoints.get(BOTTOM_LEFT).position.set(stationaryPoint.position.x - width, stationaryPoint.position.y);

				controlPoints.get(TOP_MIDDLE).position.set(stationaryPoint.position.x - width/2f, stationaryPoint.position.y + height);
				controlPoints.get(RIGHT_MIDDLE).position.set(stationaryPoint.position.x, stationaryPoint.position.y + height/2f);
				controlPoints.get(LEFT_MIDDLE).position.set(stationaryPoint.position.x - width, stationaryPoint.position.y + height/2f);
				controlPoints.get(BOTTOM_MIDDLE).position.set(stationaryPoint.position.x - width/2f, stationaryPoint.position.y);
			}


			if (stationaryPoint.id == BOTTOM_LEFT) {
				//Do the other corners

				//Have to compare it to things we are taking truth from
				boolean invertedX = controlPoints.get(TOP_RIGHT).position.x < controlPoints.get(BOTTOM_LEFT).position.x;
				boolean invertedY = controlPoints.get(TOP_RIGHT).position.y < controlPoints.get(BOTTOM_LEFT).position.y;

				if (invertedX) {
					width *= -1;
				}
				if (invertedY) {
					height *= -1;
				}

				controlPoints.get(TOP_LEFT).position.set(stationaryPoint.position.x, stationaryPoint.position.y + height);
				controlPoints.get(BOTTOM_RIGHT).position.set(stationaryPoint.position.x + width, stationaryPoint.position.y);

				controlPoints.get(TOP_MIDDLE).position.set(stationaryPoint.position.x + width/2f, stationaryPoint.position.y + height);
				controlPoints.get(RIGHT_MIDDLE).position.set(stationaryPoint.position.x + width, stationaryPoint.position.y + height/2f);
				controlPoints.get(LEFT_MIDDLE).position.set(stationaryPoint.position.x, stationaryPoint.position.y + height/2f);
				controlPoints.get(BOTTOM_MIDDLE).position.set(stationaryPoint.position.x + width/2f, stationaryPoint.position.y);
			}

			if (stationaryPoint.id == TOP_RIGHT) {
				//Do the other corners

				//Have to compare it to things we are taking truth from
				boolean invertedX = controlPoints.get(TOP_RIGHT).position.x < controlPoints.get(BOTTOM_LEFT).position.x;
				boolean invertedY = controlPoints.get(TOP_RIGHT).position.y < controlPoints.get(BOTTOM_LEFT).position.y;

				if (invertedX) {
					width *= -1;
				}
				if (invertedY) {
					height *= -1;
				}


				controlPoints.get(TOP_LEFT).position.set(stationaryPoint.position.x - width, stationaryPoint.position.y);
				controlPoints.get(BOTTOM_RIGHT).position.set(stationaryPoint.position.x, stationaryPoint.position.y - height);

				controlPoints.get(TOP_MIDDLE).position.set(stationaryPoint.position.x - width/2f, stationaryPoint.position.y);
				controlPoints.get(RIGHT_MIDDLE).position.set(stationaryPoint.position.x, stationaryPoint.position.y - height/2f);
				controlPoints.get(LEFT_MIDDLE).position.set(stationaryPoint.position.x - width, stationaryPoint.position.y - height/2f);
				controlPoints.get(BOTTOM_MIDDLE).position.set(stationaryPoint.position.x - width/2f, stationaryPoint.position.y - height);
			}


			//put them back into space
			for (ControlPoint controlPoint : controlPoints) {
				controlPoint.position.rotateDeg(transformComponent.worldRotation);
				controlPoint.position.add(transformComponent.worldPosition);
			}


			break;
		case MIDDLE:
			break;
		}


		updateTransformFromControlPoints();
	}

	private void updateTransformFromControlPoints () {
		//Oh god
		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

		for (ControlPoint controlPoint : controlPoints) {
			controlPoint.position.sub(transformComponent.worldPosition);
			controlPoint.position.rotateDeg(-transformComponent.worldRotation);
		}

		ControlPoint bottomLeftControlPoint = controlPoints.get(BOTTOM_LEFT);
		ControlPoint bottomRightControlPoint = controlPoints.get(BOTTOM_RIGHT);
		ControlPoint topRightControlPoint = controlPoints.get(TOP_RIGHT);

		//May be negative
		float totalWidth = bottomRightControlPoint.position.x - bottomLeftControlPoint.position.x;
		float totalHeight = topRightControlPoint.position.y - bottomRightControlPoint.position.y;

		//Center
		float centerX = (topRightControlPoint.position.x + bottomLeftControlPoint.position.x) / 2f;
		float centerY = (topRightControlPoint.position.y + bottomLeftControlPoint.position.y) / 2f;

		for (ControlPoint controlPoint : controlPoints) {
			controlPoint.position.rotateDeg(transformComponent.worldRotation);
			controlPoint.position.add(transformComponent.worldPosition);
		}


		Vector2 out = new Vector2();
		out.set(centerX, centerY);
		out.add(transformComponent.worldPosition);

		GameObject.setPositionFromWorldPosition(gameObject, out);
		SceneUtils.componentUpdated(gameObjectContainer, gameObject, transformComponent, true);

		if (gameObject.hasComponent(SpriteRendererComponent.class)) {
			//we set the size to the sprite's shit taking into account the scale

			Vector2 scale = transformComponent.scale;

			float totalWidthScaleConsidered = totalWidth / scale.x;
			float totalHeightScaleConsidered = totalHeight / scale.y;

			SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);
			spriteRendererComponent.size.set(totalWidthScaleConsidered, totalHeightScaleConsidered);

			SceneUtils.componentUpdated(gameObjectContainer, gameObject, spriteRendererComponent, true);
		}


	}

	private Vector2 temp = new Vector2();
	private Vector2 temp2 = new Vector2();
	private BoundingBox boundingBox = new BoundingBox();
	private BoundingBox getUnrotateRectFromOppositeCorners (ControlPoint currentManipulatingPoint, ControlPoint stationaryPoint) {
		//Find the midpoint
		float midX = (currentManipulatingPoint.position.x + stationaryPoint.position.x) / 2f;
		float midY = (currentManipulatingPoint.position.y + stationaryPoint.position.y) / 2f;

		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

		temp.set(currentManipulatingPoint.position);
		temp2.set(stationaryPoint.position);

		//Put these around their center

		temp.sub(midX, midY);
		temp2.sub(midX, midY);

		//Un-rotate these by world rot
		temp.rotateDeg(-transformComponent.worldRotation);
		temp2.rotateDeg(-transformComponent.worldRotation);

		//We should have a rect now from two opposite points, we just need to figure outn which is max


		boundingBox.clr();

		boundingBox.ext(temp.x, temp.y, 0);
		boundingBox.ext(temp2.x, temp2.y, 0);


		return boundingBox;
	}

	@Override
	public void touchUp (float x, float y) {
		super.touchUp(x, y);

		userInteracted = false;
		currentManipulatingPoint = null;
	}

	@Override
	public boolean hit (float x, float y) {
		if (!selected)
			return false;

		if (isOnTouchedPoint(x, y)) {
			return true;
		}
//		if (isOnTouchedRotationArea(x, y)) {
//			return true;
//		}

		return false;
	}

//	protected boolean isOnTouchedRotationArea (float x, float y) {
//		int touchedRA = getTouchedRotationArea(x, y);
//		return touchedRA >= -1;
//	}

	protected boolean isOnTouchedPoint (float x, float y) {
		ControlPoint touchedPoint = getTouchedPoint(x, y);
		return touchedPoint != null;
	}

	private ControlPoint getTouchedPoint (float x, float y) {
		for (ControlPoint controlPoint : controlPoints) {
			if (isPointHit(controlPoint.position, x, y)) {
				return controlPoint;
			}
		}
		return null;
	};

	private boolean isPointHit (Vector2 point, float x, float y) {
		float dst = point.dst(x, y);
		boolean hit = dst < (8 * worldPerPixel);
		return hit;
	}

	@Override
	public int getPriority () {
		return 0;
	}

	private float[] verts = new float[2 * 4];
	public void getBounds (Polygon boundingPolygon) {

		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
		SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

		// patch for negative width and height cases
		float signWidth = Math.signum(spriteRendererComponent.size.x);
		float signHeight = Math.signum(spriteRendererComponent.size.y);


		float width = signWidth * spriteRendererComponent.size.x * transformComponent.worldScale.x;
		float height = signHeight * spriteRendererComponent.size.y * transformComponent.worldScale.y;

		verts[0] = -width/2f;
		verts[1] = -height/2f;

		verts[2] = -width/2f;
		verts[3] = height/2f;

		verts[4] = width/2f;
		verts[5] = height/2f;

		verts[6] = width/2f;
		verts[7] = -height/2f;

		boundingPolygon.setPosition(transformComponent.worldPosition.x, transformComponent.worldPosition.y);
		boundingPolygon.setVertices(verts);
		boundingPolygon.setOrigin(0, 0);
		boundingPolygon.setRotation(transformComponent.worldRotation);

	}
}
