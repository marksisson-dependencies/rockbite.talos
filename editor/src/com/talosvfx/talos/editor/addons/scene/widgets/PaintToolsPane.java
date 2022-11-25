package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.PaintSurfaceGizmo;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
import com.talosvfx.talos.editor.nodes.widgets.ValueWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class PaintToolsPane extends Table implements Notifications.Observer {

    public final SquareButton paint;
    public final SquareButton erase;
    public final ColorWidget colorWidget;
    public final ValueWidget sizeWidget;
    public final ValueWidget hardnessWidget;
    public final ValueWidget opacityWidget;
    private final PaintSurfaceGizmo paintSurfaceGizmo;

    private float bracketStartCoolDown = 0f;
    private float bracketCoolDown = 0f;
    private int bracketDown = 0;

    public PaintToolsPane(PaintSurfaceGizmo paintSurfaceGizmo) {
        this.paintSurfaceGizmo = paintSurfaceGizmo;
        Notifications.registerObserver(this);

        setSkin(TalosMain.Instance().getSkin());

        paint = new SquareButton(getSkin(), getSkin().getDrawable("brush_icon"), true, "Paintbrush");
        erase = new SquareButton(getSkin(), getSkin().getDrawable("eraser_icon"), true, "Eraser");
        ButtonGroup<SquareButton> buttonButtonGroup = new ButtonGroup<>();
        buttonButtonGroup.setMaxCheckCount(1);
        buttonButtonGroup.setMinCheckCount(0);
        buttonButtonGroup.add(paint, erase);
        paint.setChecked(true);

        add(paint).padRight(10).size(37);
        add(erase).padRight(10).size(37);

        colorWidget = new ColorWidget();
        colorWidget.init(TalosMain.Instance().getSkin(), null);
        colorWidget.setColor(Color.WHITE);
        add(colorWidget).padRight(10);

        sizeWidget = createFloatWidget("size", 1, 100, 1f);
        sizeWidget.setStep(1);
        add(sizeWidget).padRight(10);
        hardnessWidget = createFloatWidget("hardness", 0, 100, 100f);
        add(hardnessWidget).padRight(10);
        opacityWidget = createFloatWidget("opacity", 0, 100, 100f);
        add(opacityWidget).padRight(10);

        pack();

        sizeWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paintSurfaceGizmo.brushTexture = null;
            }
        });
        hardnessWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paintSurfaceGizmo.brushTexture = null;
            }
        });

    }

    private ValueWidget createFloatWidget(String name, float min, float max, float value) {
        ValueWidget hardnessWidget = new ValueWidget(TalosMain.Instance().getSkin());
        hardnessWidget.setRange(min, max);
        hardnessWidget.setStep(1);
        hardnessWidget.setValue(value);
        hardnessWidget.setShowProgress(true);
        hardnessWidget.setLabel(name);

        return hardnessWidget;
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        Vector2 vec = Pools.get(Vector2.class).obtain();
        // position specifically
        Table workspace = SceneEditorAddon.get().workspaceContainer;
        workspace.localToStageCoordinates(vec.set(0, workspace.getHeight()));
        setPosition(vec.x + 25, vec.y - getHeight() - 25);
        Pools.get(Vector2.class).free(vec);

        if(bracketDown > 0) {
            bracketStartCoolDown -= Gdx.graphics.getDeltaTime();

            if(bracketStartCoolDown <= 0) {
                bracketStartCoolDown = 0f;

                bracketCoolDown -= Gdx.graphics.getDeltaTime();
                if(bracketCoolDown <= 0) {
                    bracketCoolDown = 0.1f;
                    if (bracketDown == Input.Keys.LEFT_BRACKET) {
                        decreaseSize();
                    } else if (bracketDown == Input.Keys.RIGHT_BRACKET) {
                        increaseSize();
                    }
                }
            }
        }
    }

    public void setFrom(GameObject gameObject) {

    }

    public int getSize() {
        return (int)sizeWidget.getValue().floatValue();
    }

    public float getHardness() {
        return hardnessWidget.getValue()/100f;
    }

    public Color getColor() {
        return colorWidget.getValue();
    }

    private float getSizeDiff() {
        float size = getSize();
        float diff = 1;
        if(size >= 10) diff = 5;
        if(size >= 50) diff = 10;

        return diff;
    }

    public void decreaseSize() {
        float size = getSize();
        float diff = getSizeDiff();
        float newSize = (float) (Math.floor(size / diff) * diff);
        if(size == newSize) size -= diff; else size = newSize;
        sizeWidget.setValue(size);
    }

    public void increaseSize() {
        float size = getSize();
        float diff = getSizeDiff();
        float newSize = (float) (Math.ceil(size / diff) * diff);
        if(size == newSize) size += diff; else size = newSize;
        sizeWidget.setValue(size);
    }

    public void bracketDown(int keycode) {
        bracketDown = keycode;
        bracketStartCoolDown = 0.5f;
        bracketCoolDown = 0f;

        if (keycode == Input.Keys.LEFT_BRACKET) {
            decreaseSize();
        } else if (keycode == Input.Keys.RIGHT_BRACKET) {
            increaseSize();
        }
    }

    public void bracketUp(int keycode) {
        bracketDown = 0;
        bracketStartCoolDown = 0;
        bracketCoolDown = 0f;
    }
}
