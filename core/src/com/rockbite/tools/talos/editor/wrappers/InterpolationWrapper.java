package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.rockbite.tools.talos.runtime.modules.InterpolationModule;

public class InterpolationWrapper extends ModuleWrapper<InterpolationModule> {

    IntMap<Interpolation> map;
    ObjectMap<String, Integer> names;

    public InterpolationWrapper() {
        super();
    }

    @Override
    protected float reportPrefWidth() {
        return 250;
    }

    @Override
    protected void configureSlots() {
        map = new IntMap<>();
        names = new ObjectMap<>();
        Array<String> namesArr = new Array<>();
        // get list of possible interpolations

        Field[] fields = ClassReflection.getFields(Interpolation.class);
        int iter = 0;
        for(int i = 0; i < fields.length; i++) {
            if(fields[i].getType().isAssignableFrom(Interpolation.class)) {
                try {
                    Interpolation interp = (Interpolation) fields[i].get(null);
                    names.put(fields[i].getName(), iter);
                    map.put(iter++, interp);
                    namesArr.add(fields[i].getName());
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
            }
        }

        addInputSlot("alpha (0 to 1)", InterpolationModule.ALPHA);

        addOutputSlot("output", 0);


        final VisSelectBox<String> selectBox = addSelectBox(namesArr);

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedString = selectBox.getSelected();
                Interpolation interp = map.get(names.get(selectedString));

                module.setInterpolation(interp);
            }
        });
    }
}
