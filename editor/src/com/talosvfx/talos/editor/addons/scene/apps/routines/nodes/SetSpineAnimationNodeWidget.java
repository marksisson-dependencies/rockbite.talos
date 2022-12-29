package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.nodes.widgets.SelectWidget;
import com.talosvfx.talos.editor.project2.SharedResources;

public class SetSpineAnimationNodeWidget extends AbstractRoutineNodeWidget {

    private SelectWidget interpolationSelectBox;

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);
        interpolationSelectBox = new SelectWidget();
        interpolationSelectBox.init(SharedResources.skin);

        String variableName = "animation";
        widgetMap.put(variableName, interpolationSelectBox);
        typeMap.put(variableName, "string");
        defaultsMap.put(variableName, "linear");
        Table container = getCustomContainer("animation_list");
        container.add(interpolationSelectBox).growX().row();

        GameAssetWidget reference = (GameAssetWidget)getWidget("reference");

        reference.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                loadList(reference);
            }
        });
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        super.read(json, jsonValue);

        GameAssetWidget reference = (GameAssetWidget)getWidget("reference");
        if(reference != null) {
            loadList(reference);
        }
    }

    private void loadList(GameAssetWidget reference) {
        GameAsset<SkeletonData> value = reference.getValue();
        SkeletonData skeletonData = value.getResource();
        Array<Animation> animations = skeletonData.getAnimations();
        Array<String> names = new Array<>();
        for(Animation animation: animations) {
            names.add(animation.getName());
        }
        interpolationSelectBox.setOptions(names);
    }
}
