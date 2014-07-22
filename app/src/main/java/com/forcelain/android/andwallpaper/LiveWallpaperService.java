package com.forcelain.android.andwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;

/**
 * Created by aglyzin on 18.07.14.
 */
public class LiveWallpaperService extends BaseLiveWallpaperService {

    private static int CAMERA_WIDTH = 480;
    private static int CAMERA_HEIGHT = 720;

    private Camera camera;
    private TextureRegion vaderRegion;
    private TextureRegion saberRegion;
    private Sprite vader;
    private Sprite saber;
    private float saberLevel = 1;

    @Override
    public EngineOptions onCreateEngineOptions() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        adjustScreenSize(displayMetrics);
        EngineOptions options = new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);
        options.getRenderOptions().setMultiSampling(true);
        return options;
    }

    @Override
    public void onSurfaceChanged(GLState pGLState, int pWidth, int pHeight) {
        super.onSurfaceChanged(pGLState, pWidth, pHeight);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        adjustScreenSize(displayMetrics);
        adjustScene();
    }

    private void adjustScreenSize(DisplayMetrics displayMetrics) {
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        wm.getDefaultDisplay().getRotation();
        CAMERA_WIDTH = displayMetrics.widthPixels;
        CAMERA_HEIGHT = displayMetrics.heightPixels;
        if (camera == null){
            camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        } else {
            camera.set(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        }
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(getTextureManager(), 2048, 2048, TextureOptions.BILINEAR);
        vaderRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "vader.png", 0, 0);
        saberRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "saber.png", 0, 1025);
        textureAtlas.load();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        Scene scene = new Scene();
        scene.setBackground(new Background(0, 0, 0));

        vader = new Sprite(0, 0, vaderRegion, getVertexBufferObjectManager());
        saber = new Sprite(0, 0, saberRegion, getVertexBufferObjectManager());
        adjustScene();

        scene.attachChild(vader);
        scene.attachChild(saber);

        /*
        scene.registerUpdateHandler(new TimerHandler(1, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                int rssi = wifiManager.getConnectionInfo().getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 10);
                saberLevel = level/10.0f;
                saber.setScale(saberLevel, 1f);
            }
        }));
        */

        pOnCreateSceneCallback.onCreateSceneFinished(scene);

    }

    private void adjustScene() {
        float minSize = Math.min(CAMERA_HEIGHT, CAMERA_WIDTH);
        float vaderSize = minSize / 1.5f;
        float factor = 1024 / vaderSize;
        vader.setHeight(vaderSize);
        vader.setWidth(vaderSize);
        vader.setPosition(CAMERA_WIDTH / 2 - vaderSize / 2, CAMERA_HEIGHT / 2 - vaderSize / 2);
        float saberOffsetX = 234 / factor;
        float saberOffsetY = 60 / factor;
        float saberAnchor = CAMERA_WIDTH/2 + saberOffsetX;
        float saberAnchorY = CAMERA_HEIGHT/2 + saberOffsetY - saber.getHeight()/2;
        float angle = -65;
        saber.setPosition(saberAnchor, saberAnchorY);
        saber.setRotationCenter(0, saber.getHeight()/2);
        saber.setRotation(angle);
        saber.setScaleCenter(0, saber.getHeight()/2);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    public synchronized void onResumeGame() {
        super.onResumeGame();
        //registerReceiver(batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //registerReceiver(wifiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
    }

    @Override
    public void onPauseGame() {
        super.onPauseGame();
        //unregisterReceiver(batInfoReceiver);
        //unregisterReceiver(wifiReceiver);
    }

    private BroadcastReceiver batInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            saberLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) / (float)100;
            saber.setScale(saberLevel, 1f);
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            Log.d("@@@@", "wifi");
        }
    };
}
