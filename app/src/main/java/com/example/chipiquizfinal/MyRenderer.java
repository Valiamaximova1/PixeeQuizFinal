package com.example.chipiquizfinal;

import static android.transition.Scene.getCurrentScene;


import static org.rajawali3d.util.RawShaderLoader.mContext;

import android.content.Context;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.renderer.*;


public class MyRenderer extends RajawaliRenderer {

    public MyRenderer(Context context) {
        super(context);
        // Optionally set other parameters like frame rate.
    }

    @Override
    protected void initScene() {
        // Set up a basic scene including background color
        getCurrentScene().setBackgroundColor(0xffeeeeee);

        // Configure the camera:
        getCurrentCamera().setPosition(0, 0, 4);
        getCurrentCamera().setLookAt(0, 0, 0);

        // Optionally set up a light source:
        DirectionalLight light = new DirectionalLight(1.0, 0.2, -1.0);
        light.setPower(2);
        getCurrentScene().addLight(light);

        // Load a 3D model (e.g., OBJ file):
        try {
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.my_model_obj);
            objParser.parse();
            Object3D model = objParser.getParsedObject();
            getCurrentScene().addChild(model);
        } catch (ParsingException e) {
            e.printStackTrace();
        }
    }
}
