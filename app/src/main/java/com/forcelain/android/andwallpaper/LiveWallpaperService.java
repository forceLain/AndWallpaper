package com.forcelain.android.andwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.opengl.GLES20;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.particle.BatchedSpriteParticleSystem;
import org.andengine.entity.particle.Particle;
import org.andengine.entity.particle.ParticleSystem;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.IParticleEmitter;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.AlphaParticleInitializer;
import org.andengine.entity.particle.initializer.BlendFunctionParticleInitializer;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.ScaleParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.BaseDoubleValueSpanParticleModifier;
import org.andengine.entity.particle.modifier.BaseSingleValueSpanParticleModifier;
import org.andengine.entity.particle.modifier.ColorParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.util.modifier.ease.EaseBackIn;
import org.andengine.util.modifier.ease.EaseBackInOut;
import org.andengine.util.modifier.ease.EaseElasticIn;
import org.andengine.util.modifier.ease.EaseElasticInOut;
import org.andengine.util.modifier.ease.EaseElasticOut;
import org.andengine.util.modifier.ease.EaseStrongInOut;
import org.andengine.util.modifier.ease.IEaseFunction;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aglyzin on 18.07.14.
 */
public class LiveWallpaperService extends BaseLiveWallpaperService {

    private static final float BLINK_DURATION_SLOW = 3;
    private static final float BLINK_DURATION_MEDIUM = 1;
    private static final float BLINK_DURATION_FAST = 0.3f;
    private static int CAMERA_WIDTH = 480;
    private static int CAMERA_HEIGHT = 720;

    private Camera camera;
    private TextureRegion vaderRegion;
    private TextureRegion saberRegion;
    private TextureRegion particleTextureRegion;
    private Sprite vader;
    private Sprite saber;
    private float saberLevel = 1;
    private ParticleSystem particleSystem;
    private float factor = 1;

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
        BitmapTextureAtlas particleAtlas = new BitmapTextureAtlas(getTextureManager(), 32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        vaderRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "vader.png", 0, 0);
        saberRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "saber.png", 0, 1025);
        particleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(particleAtlas, this, "particle_fire.png", 0, 0);
        textureAtlas.load();
        particleAtlas.load();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        Scene scene = new Scene();
        scene.setBackground(new Background(0, 0, 0));

        vader = new Sprite(0, 0, vaderRegion, getVertexBufferObjectManager());
        saber = new Sprite(0, 0, saberRegion, getVertexBufferObjectManager());
        SmokeParticleSystem smokeParticleSystem = new SmokeParticleSystem();
        particleSystem = smokeParticleSystem.build();
        adjustScene();


        scene.attachChild(vader);
        scene.attachChild(saber);
        particleSystem.setParticlesSpawnEnabled(true);
        scene.attachChild(particleSystem);

        pOnCreateSceneCallback.onCreateSceneFinished(scene);
    }

    private void adjustScene() {
        float minSize = Math.min(CAMERA_HEIGHT, CAMERA_WIDTH);
        float vaderSize = minSize / 2f;
        factor = 1024 / vaderSize;
        vader.setHeight(vaderSize);
        vader.setWidth(vaderSize);
        vader.setPosition(CAMERA_WIDTH / 2 - vaderSize / 2, CAMERA_HEIGHT / 2 - vaderSize / 2);
        float saberOffsetX = 234 / factor;
        float saberOffsetY = 60 / factor;

        float smokeOffsetX = (208) / factor - 16;
        float smokeOffsetY = (8) / factor - 16;

        particleSystem.reset();
        PointParticleEmitter particleEmitter = (PointParticleEmitter) particleSystem.getParticleEmitter();
        particleEmitter.setCenter(CAMERA_WIDTH/2 + smokeOffsetX, CAMERA_HEIGHT/2 + smokeOffsetY);

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String linkTo = prefs.getString(SettingsActivity.LINK_MODE, "battery");
        if ("battery".equals(linkTo)){
            registerReceiver(batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } else if ("wifi".equals(linkTo)) {
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        } else {
            saberLevel = 1;
            saber.setScale(saberLevel / factor, 1 / factor);
        }

        String blink = prefs.getString(SettingsActivity.BLINK, "slow");
        if ("slow".equals(blink)){
            setBlink(BLINK_DURATION_SLOW);
        } else if ("medium".equals(blink)){
            setBlink(BLINK_DURATION_MEDIUM);
        } else if ("fast".equals(blink)){
            setBlink(BLINK_DURATION_FAST);
        } else {
            saber.clearEntityModifiers();
            saber.setAlpha(1);
        }

        boolean showSmoke = prefs.getBoolean(SettingsActivity.SMOKE, true);
        particleSystem.setParticlesSpawnEnabled(showSmoke);
    }

    @Override
    public void onPauseGame() {
        super.onPauseGame();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String linkTo = prefs.getString(SettingsActivity.LINK_MODE, "battery");
        if ("battery".equals(linkTo)){
            unregisterReceiver(batInfoReceiver);
        } else if ("wifi".equals(linkTo)){
            unregisterReceiver(wifiReceiver);
        }
    }


    private void setBlink(float blinkDuration) {
        AlphaModifier in = new AlphaModifier(blinkDuration, 0.3f, 1f);
        AlphaModifier out = new AlphaModifier(blinkDuration, 1f, 0.3f);
        SequenceEntityModifier sequenceEntityModifier = new SequenceEntityModifier(in, out);
        LoopEntityModifier loopEntityModifier = new LoopEntityModifier(sequenceEntityModifier);
        saber.clearEntityModifiers();
        saber.registerEntityModifier(loopEntityModifier);
    }


    private BroadcastReceiver batInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            saberLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) / (float)100;
            saber.setScale(saberLevel/factor, 1f/factor);
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            int rssi = wifiManager.getConnectionInfo().getRssi();
            int level = WifiManager.calculateSignalLevel(rssi, 10);
            saberLevel = level/10.0f;
            saber.setScale(saberLevel/factor, 1f/factor);
        }
    };

    private class SmokeParticleSystem {

        private static final float RATE_MIN    = 10;
        private static final float RATE_MAX	   = 20;
        private static final int PARTICLES_MAX = 100;

        public ParticleSystem build() {

            PointParticleEmitter pointParticleEmitter = new PointParticleEmitter(0, 0);
            SpriteParticleSystem particleSystem = new SpriteParticleSystem (pointParticleEmitter, RATE_MIN, RATE_MAX, PARTICLES_MAX, particleTextureRegion, getVertexBufferObjectManager());
            particleSystem.addParticleInitializer(new ColorParticleInitializer<Sprite>(0.3f, 0.3f, 0.3f));
            particleSystem.addParticleInitializer(new AlphaParticleInitializer<Sprite>(0));
            particleSystem.addParticleInitializer(new ScaleParticleInitializer<Sprite>(0.1f));
            particleSystem.addParticleInitializer(new BlendFunctionParticleInitializer<Sprite>(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE));
            particleSystem.addParticleInitializer(new VelocityParticleInitializer<Sprite>(0, 0, -15, -10));
            particleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(6));

            particleSystem.addParticleModifier(new ScaleParticleModifier<Sprite>(0, 5, 0.1f, 0.7f));
            particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0, 1, 0, 1));
            particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(5, 6, 1, 0));
            particleSystem.addParticleModifier(new TranslateXParticleModifier<Sprite>(1, 2, 0, -7));
            particleSystem.addParticleModifier(new TranslateXParticleModifier<Sprite>(2, 4, -7, 7));
            particleSystem.addParticleModifier(new TranslateXParticleModifier<Sprite>(4, 6, 7, -7));

            return particleSystem;
        }
    }

    private class TranslateXParticleModifier<T extends IEntity> extends BaseSingleValueSpanParticleModifier<T>{

        float x;

        public TranslateXParticleModifier(float pFromTime, float pToTime, float pFromValue, float pToValue, IEaseFunction pEaseFunction) {
            super(pFromTime, pToTime, pFromValue, pToValue, pEaseFunction);
        }

        public TranslateXParticleModifier(float pFromTime, float pToTime, float pFromValue, float pToValue) {
            super(pFromTime, pToTime, pFromValue, pToValue);
        }

        @Override
        protected void onSetInitialValue(Particle pParticle, float pValue) {
            x = pParticle.getEntity().getX();
        }

        @Override
        protected void onSetValue(Particle pParticle, float pPercentageDone, float pValue) {
            pParticle.getEntity().setX(x+pValue);
        }
    }
}
