package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

//import io.github.sceneview.SceneView;
//import io.github.sceneview.node.ModelNode;

public class ArModelViewerActivity extends AppCompatActivity {

//    private SceneView sceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Създаване на SceneView
//        sceneView = new SceneView(this);
//        setContentView(sceneView);
//
//        // Зареждане на модел синхронно (без coroutines, без позиция)
//        ModelNode modelNode = new ModelNode();
//        modelNode.loadModel(this, "models/old_computer_ram.glb", true, 0.3f);
//        sceneView.addChild(modelNode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (sceneView != null) {
//            sceneView.destroy();
//        }
    }
}
