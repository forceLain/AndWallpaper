package com.forcelain.android.andwallpaper;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;

/**
 * Created by aglyzin on 18.07.14.
 */
public class LiveWallpaperService extends BaseLiveWallpaperService {

    private static int CAMERA_WIDTH = 480;
    private static int CAMERA_HEIGHT = 720;

    private Camera mCamera;
    private Scene mScene;

    @Override
    public EngineOptions onCreateEngineOptions() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        wm.getDefaultDisplay().getRotation();
        CAMERA_WIDTH = displayMetrics.widthPixels;
        CAMERA_HEIGHT = displayMetrics.heightPixels;

        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        mScene= new Scene();

        mScene.setBackground(new Background(0.0f, 0.0f, 0.0f));

        pOnCreateSceneCallback.onCreateSceneFinished(mScene);

    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }
}
