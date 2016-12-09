package dhare.musictiles.Game;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.RectangleParticleEmitter;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.color.Color;

import java.util.ArrayList;
import java.util.Iterator;

import dhare.musictiles.Main.MainList;
import dhare.musictiles.MusicControl.MusicService;
import dhare.musictiles.NoteMethods.NoteMethods;
import dhare.musictiles.NoteMethods.Song;

public class GameActivity extends BaseGameActivity {
    protected static final int CAMERA_WIDTH = 480;
    protected static final int CAMERA_HEIGHT = 800;
    protected static final int QUARTER_WIDTH = 480 / 4;
    protected static final int NOTE_HEIGHT = 150;

    private TextureRegion playTR;
    private TextureRegion nextTR;
    private TextureRegion previousTR;
    private TextureRegion backTR;
    private TextureRegion pauseTR;

    private TextureRegion mParticleTexture;
    private RectangleParticleEmitter mEmitter;
    private SpriteParticleSystem mSystem;

    private Font fnt;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = true;
    private ArrayList<Song> songList;

    @Override
    public EngineOptions onCreateEngineOptions() {
        //analyseSong("");
        n.paused = true;
        n.createNoteTimes(getDuration());
        Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions options = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), mCamera);
        //EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), mCamera);
        options.getTouchOptions().setNeedsMultiTouch(true);
        n.CAMERA_HEIGHT = CAMERA_HEIGHT;
        n.QUARTER_WIDTH = QUARTER_WIDTH;
        n.CAMERA_WIDTH = CAMERA_WIDTH;
        n.NOTE_HEIGHT = NOTE_HEIGHT;
        //n.VBO = this.getVertexBufferObjectManager();
        return options;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        loadGFX();
        //FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
        fnt = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 256, 256, this.getAssets(), "fnt/basic.TTF", 46, true, android.graphics.Color.BLACK);
        fnt.load();

        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    //private void analyseSong(String realPathFromURI) {

    //}

    private void loadGFX() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        //nearest
        BitmapTextureAtlas b = new BitmapTextureAtlas(this.getTextureManager(), 256, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        //TODO: make sizes bigger for less stretch
        playTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "play.png", 0, 0);
        previousTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "prev.png", 69, 0);
        nextTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "next.png", 30, 0);
        backTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "back.png", 108, 0);
        pauseTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "pause.png", 156, 0);
        //this.mEngine.getTextureManager().loadTexture(b);
        b.load();
        /*BitmapTextureAtlas mParticleAtlas = new BitmapTextureAtlas(this.getTextureManager(), 8, 8);
        mParticleTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mParticleAtlas, this, "prtcl.png", 0, 0);
        mParticleAtlas.load();*/
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        n.scene = new Scene();
        n.scene.setBackground(new Background(Color.WHITE));
        n.scene.setBackgroundEnabled(true);
        n.scene.registerUpdateHandler(new IUpdateHandler() {
            public void reset() {
            }

            public void onUpdate(float pSecondsElapsed) {
                update();
            }
        });
        pOnCreateSceneCallback.onCreateSceneFinished(n.scene);
    }

    @Override
    public void onPopulateScene(final Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {

        Text t = new Text(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, fnt, "Score:0123", this.getVertexBufferObjectManager());
        t.setVisible(false);
        t.setZIndex(4);
        createButtons(pScene, t.getTag());
        drawGoal(pScene);
        drawLines(pScene);
        drawMenuBar(pScene);
        createTouchAreas(pScene);
        pScene.attachChild(t);
        pScene.sortChildren();
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    private void drawMenuBar(Scene pScene) {
        Rectangle rect = new Rectangle(0, 0, CAMERA_WIDTH, 40, this.getVertexBufferObjectManager());
        rect.setColor(Color.WHITE);
        rect.setZIndex(3);
        pScene.attachChild(rect);
        Line l = new Line(0, 40, CAMERA_WIDTH, 40, this.getVertexBufferObjectManager());
        l.setLineWidth(5);
        l.setColor(Color.BLACK);
        l.setZIndex(3);
        pScene.attachChild(l);
    }

    private void createTouchAreas(final Scene pScene) {
        for (int i = 0; i < 4; i++) {
            final int x = i;
            Rectangle r = new Rectangle(QUARTER_WIDTH * i, CAMERA_HEIGHT - (NOTE_HEIGHT + 30), QUARTER_WIDTH, (NOTE_HEIGHT + 30), this.getVertexBufferObjectManager()) {
                @Override
                public boolean onAreaTouched(final TouchEvent pAreaTouchEvent,
                                             final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                    switch (pAreaTouchEvent.getAction()) {
                        case TouchEvent.ACTION_DOWN:
                            if (n.allLanes.get(x).size() > 0 && n.allLanes.get(x).get(0).getY() > CAMERA_HEIGHT - NOTE_HEIGHT - 20 && (n.allLanes.get(x).get(0).getY() + NOTE_HEIGHT) < CAMERA_HEIGHT + 20) {
                                final Rectangle r = new Rectangle(QUARTER_WIDTH * x, n.allLanes.get(x).get(0).getY(), QUARTER_WIDTH, NOTE_HEIGHT, this.getVertexBufferObjectManager());
                                n.allLanes.get(x).get(0).detachSelf();
                                n.allLanes.get(x).remove(0);
                                r.setColor(Color.BLACK);
                                r.setZIndex(0);
                                pScene.attachChild(r);
                                pScene.sortChildren();
                                //createParticles(x, pScene);
                                mEngine.registerUpdateHandler(new TimerHandler(0.01f, new ITimerCallback() {
                                    Float alpha = 1f;

                                    @Override
                                    public void onTimePassed(TimerHandler pTimerHandler) {
                                        alpha -= 0.2f;
                                        r.setAlpha(alpha);
                                        if (alpha > 0.2f)
                                            pTimerHandler.reset();
                                        else {
                                            r.detachSelf();
                                            alpha = 1f;
                                        }
                                        //n.allLanes.get(i).get(0).detachSelf();
                                        //n.allLanes.get(x).remove(0);
                                    }
                                }));
                            } else {
                                n.destoryNotes();
                            }
                            break;
                        case TouchEvent.ACTION_UP:
                    }

                    return super.
                            onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
                }
            };
            r.setColor(0, 0, 0, 0);
            pScene.attachChild(r);
            pScene.registerTouchArea(r);
        }
    }

    private void createParticles(int x, final Scene pScene) {
        /*mEmitter = new RectangleParticleEmitter(n.allLanes.get(x).get(0).getX(), n.allLanes.get(x).get(0).getY(), QUARTER_WIDTH, NOTE_HEIGHT);
        mSystem = new SpriteParticleSystem(mEmitter, 5f, 10f, 15, mParticleTexture, getVertexBufferObjectManager());
        mSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(2.0f));
        mSystem.addParticleInitializer(new BlendFunctionParticleInitializer<Sprite>(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA));
        mSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0.0f, 1.99f, 1.0f, 0f));
        pScene.attachChild(mSystem);
        mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                mSystem.setParticlesSpawnEnabled(false);
                pScene.unregisterUpdateHandler(pTimerHandler);
            }
        }));
        mEngine.registerUpdateHandler(new TimerHandler(4f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                pScene.detachChild(mSystem);
                pScene.unregisterUpdateHandler(pTimerHandler);
            }
        }));*/
    }

    private void drawGoal(Scene pScene) {
        Rectangle rect = new Rectangle(0, CAMERA_HEIGHT - NOTE_HEIGHT, CAMERA_WIDTH, NOTE_HEIGHT, this.getVertexBufferObjectManager());
        rect.setColor(createColor(71, 114, 255, 100)); //TODO: fix color
        rect.setZIndex(1);
        pScene.attachChild(rect);
    }

    private Color createColor(int r, int g, int b, int a) {
        return new Color((float) r / 255, (float) g / 255, (float) b / 255, (float) a / 255);
    }

    private void createButtons(final Scene pScene, final int tTag) {
        /*playTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "play.png", 0, 0);
        previousTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "prev.png", 69, 0);
        nextTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "next.png", 30, 0);
        backTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "back.png", 108, 0);
        pauseTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(b, this, "pause.png", 156, 0);*/

        float scale = 39 / 32;

        Sprite backBtn = new Sprite(2, 5, backTR, mEngine.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        break;
                    case TouchEvent.ACTION_UP:
                        pause();
                        Intent i = new Intent(GameActivity.this, MainList.class);
                        //i.putParcelableArrayListExtra("songList", songList);
                        //i.putExtra("index", Integer.parseInt(view.getTag().toString()));
                        startActivity(i);
                        finish();
                        //TODO: dispose of sprites?, end class
                }
                return super.onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        backBtn.setScale(scale, scale);
        backBtn.setZIndex(4);
        pScene.registerTouchArea(backBtn);
        pScene.attachChild(backBtn);

        Sprite nextBtn = new Sprite(0, 5, nextTR, mEngine.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        break;
                    case TouchEvent.ACTION_UP:
                        playNext();
                }
                return super.onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        nextBtn.setScale(scale, scale);
        nextBtn.setX(CAMERA_WIDTH - nextBtn.getWidth() - 2);
        nextBtn.setZIndex(4);
        pScene.registerTouchArea(nextBtn);
        pScene.attachChild(nextBtn);

        final int quadrant = (int) ((nextBtn.getX() - backBtn.getX() + backBtn.getWidth()) / 3);

        Sprite prevBtn = new Sprite(0, 5, previousTR, mEngine.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        break;
                    case TouchEvent.ACTION_UP:
                        playPrev();
                }
                return super.onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        prevBtn.setScale(scale, scale);
        prevBtn.setX(quadrant * 1 - prevBtn.getWidth() / 2);
        prevBtn.setZIndex(4);
        pScene.registerTouchArea(prevBtn);
        pScene.attachChild(prevBtn);

        Sprite playBtn = new Sprite(0, 5, playTR, mEngine.getVertexBufferObjectManager()) {
            Boolean startedCountDownToPlay = false;

            @Override
            public boolean onAreaTouched(final TouchEvent pAreaTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                switch (pAreaTouchEvent.getAction()) {
                    case TouchEvent.ACTION_DOWN:
                        break;
                    case TouchEvent.ACTION_UP:
                        if (playbackPaused) {
                            playbackPaused = false;
                            startedCountDownToPlay = true;
                            Text t = (Text) pScene.getChildByTag(tTag);
                            t.setText("3");
                            //t.setVisible(true);
                            pScene.sortChildren();
                            mEngine.registerUpdateHandler(new TimerHandler(1, new ITimerCallback() {
                                int x = 4;
                                Text txt = (Text) pScene.getChildByTag(tTag);

                                @Override
                                public void onTimePassed(TimerHandler pTimerHandler) {
                                    x -= 1;
                                    if (x == 0) {
                                        startedCountDownToPlay = false;
                                        txt.setVisible(false);
                                        start();
                                    } else {
                                        txt.setVisible(false);
                                        txt.setText(String.valueOf(x));
                                        txt.setPosition((CAMERA_WIDTH / 2) - (txt.getWidth() / 2), (CAMERA_HEIGHT / 2) - (txt.getHeight() / 2));
                                        txt.setVisible(true);
                                        pTimerHandler.reset();
                                    }
                                }
                            }));
                        } else {
                            if (startedCountDownToPlay) {
                                Text txt = (Text) pScene.getChildByTag(tTag);
                                txt.setVisible(false);
                            }
                            pause();
                        }
                }
                return super.onAreaTouched(pAreaTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        playBtn.setScale(scale, scale);
        playBtn.setX(quadrant * 2 - playBtn.getWidth() / 2);
        playBtn.setZIndex(4);
        pScene.registerTouchArea(playBtn);
        pScene.attachChild(playBtn);
    }

    private void drawLines(Scene pScene) {
        for (int i = 1; i <= 4; i++) {
            Line l = new Line(QUARTER_WIDTH * i, 0, QUARTER_WIDTH * i, CAMERA_HEIGHT, this.getVertexBufferObjectManager());
            l.setLineWidth(5);
            l.setColor(Color.BLACK);
            l.setZIndex(2);
            pScene.attachChild(l);
        }
    }

    NoteMethods n = new NoteMethods();

    public void update() {
        n.moveNotes();
        n.addNotes(getCurrentPosition());
        n.destoryNotes();
    }
    //TODO: remove ties to a 'controller'

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            Intent i = getIntent();
            songList = i.getParcelableArrayListExtra("songList");
            musicSrv.setList(songList);
            musicBound = true;
            musicSrv.setSong(i.getIntExtra("index", 0));
            if (!musicSrv.playSong())
                new AlertDialog.Builder(GameActivity.this)
                        .setTitle("Error")
                        .setMessage("Could not play selected file")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton("Done", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(GameActivity.this, MainList.class);
                                //i.putParcelableArrayListExtra("songList", songList);
                                //i.putExtra("index", Integer.parseInt(view.getTag().toString()));
                                startActivity(i);
                                finish();
                            }
                        }).show();
            else {
                playbackPaused = false;
                n.paused = false;
            }
            //analyseSong(musicSrv.getRealPathFromURI());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void start() {
        musicSrv.go();
        playbackPaused = false;
        n.paused = false;
    }

    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    public void pause() {
        playbackPaused = true;
        n.paused = true;
        musicSrv.pausePlayer();
    }

    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return false;
    }

    public boolean canSeekBackward() {
        return false;
    }

    public boolean canSeekForward() {
        return false;
    }

    public int getAudioSessionId() {
        return 0;
    }

    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            playbackPaused = false;
            n.paused = false;
        }

        for (int i = 0; i < 4; i++) {
            Iterator<Rectangle> rect = n.allLanes.get(i).iterator();
            while (rect.hasNext()) {
                //n.allLanes.get(i).get(x).dispose();
                //n.allLanes.get(i).get(x).detachSelf();
                n.scene.detachChild(rect.next());
                rect.remove();
            }
        }
    }


    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            playbackPaused = false;
            n.paused = false;
        }

        for (int i = 0; i < 4; i++) {
            Iterator<Rectangle> rect = n.allLanes.get(i).iterator();
            while (rect.hasNext()) {
                //n.allLanes.get(i).get(x).dispose();
                //n.allLanes.get(i).get(x).detachSelf();
                n.scene.detachChild(rect.next());
                rect.remove();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
        //musicSrv.stopSelf();
    }
}
