package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.ScriptMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.addons.scene.widgets.property.AssetSelectWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class ScriptComponent extends AComponent implements Json.Serializable, GameResourceOwner<String> {

    GameAsset<String> scriptResource;

    Array<PropertyWrapper<?>> scriptProperties = new Array<>();

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        AssetSelectWidget<String> widget = new AssetSelectWidget<String>("Script", GameAssetType.SCRIPT, new Supplier<GameAsset<String>>() {
            @Override
            public GameAsset<String> get () {
                return scriptResource;
            }
        }, new PropertyWidget.ValueChanged<GameAsset<String>>() {
            @Override
            public void report (GameAsset<String> value) {
                setGameAsset(value);
            }
        });

        properties.add(widget);

        for (PropertyWrapper<?> scriptProperty : scriptProperties) {
            PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(scriptProperty);
            generate.setParent(this);
            properties.add(generate);
        }

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Script Component";
    }

    @Override
    public int getPriority () {
        return 4;
    }

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SCRIPT;
    }

    @Override
    public GameAsset<String> getGameResource () {
        return scriptResource;
    }

    @Override
    public void setGameAsset (GameAsset<String> gameAsset) {
        this.scriptResource = gameAsset;
        scriptProperties.clear();
        importScriptPropertiesFromMeta(false);
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);
        json.writeValue("properties", scriptProperties);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);

        loadScriptFromIdentifier(gameResourceIdentifier);

        scriptProperties.clear();
        JsonValue propertiesJson = jsonData.get("properties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                scriptProperties.add(json.readValue(PropertyWrapper.class, propertyJson));
            }
        }
    }

    private void loadScriptFromIdentifier (String gameResourceIdentifier) {
        GameAsset<String> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SCRIPT);
        setGameAsset(assetForIdentifier);
    }

    public void importScriptPropertiesFromMeta (boolean tryToMerge) {
        Array<PropertyWrapper<?>> copyWrappers = new Array<>();
        copyWrappers.addAll(scriptProperties);

        scriptProperties.clear();
        if (getGameResource() != null) {
            ScriptMetadata metadata = ((ScriptMetadata) getGameResource().getRootRawAsset().metaData);
            for (PropertyWrapper<?> propertyWrapper : metadata.scriptPropertyWrappers) {
                scriptProperties.add(propertyWrapper.clone());
            }
        }

        if (tryToMerge) {
            for (PropertyWrapper copyWrapper : copyWrappers) {
                for (PropertyWrapper scriptProperty : scriptProperties) {
                    if (copyWrapper.propertyName.equals(scriptProperty.propertyName) && copyWrapper.getClass().equals(scriptProperty.getClass())) {
                        scriptProperty.setValue(copyWrapper.getValue());
                        break;
                    }
                }
            }

            for (PropertyWrapper<?> scriptProperty : scriptProperties) {
                if (scriptProperty.getValue() == null) {
                    scriptProperty.setDefault();
                }
            }

        } else {
            for (PropertyWrapper<?> scriptProperty : scriptProperties) {
                scriptProperty.setDefault();
            }
        }
    }

    @Override
    public boolean allowsMultipleOfTypeOnGameObject () {
        return true;
    }
}