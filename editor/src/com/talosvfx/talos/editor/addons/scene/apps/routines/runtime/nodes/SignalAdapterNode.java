package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class SignalAdapterNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        sendSignal("out");
    }
}
