package redmaple.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import redmaple.GameSettings;
import redmaple.RedMaple;
import redmaple.audio.AudioProcessor;
import redmaple.map.Map;
import redmaple.map.SpriteBlock;
import redmaple.mapgen.Block;
import redmaple.mapgen.DefaultMapGenerator;
import redmaple.mapgen.MapGenerator;
import redmaple.mapgen.NoteBlock;
import redmaple.player.Player;
import redmaple.songfinder.StoredMusic;
import redmaple.util.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 20.3.2013
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public class GameScreen implements Screen, InputProcessor {

    public RedMaple redMaple;
    private StoredMusic music;
    public AudioProcessor audioProcessor;

    private ThreadPoolExecutor threadPool;
    private ThreadPoolExecutor musicPool;

    private void initializeThreadpools() {

        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        {
            threadPool.setThreadFactory(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                }
            });
        }
        musicPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        {
            musicPool.setThreadFactory(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setPriority(Thread.MAX_PRIORITY);
                    t.setDaemon(true);
                    return t;
                }
            });
        }

    }

    {
        initializeThreadpools();
    }

    public ShapeRenderer shapeRenderer;
    public SpriteBatch batch;
    public OrthographicCamera camera;

    public SpectrumDrawer spectrumDrawer;
    public FluxGraphDrawer fluxGraphDrawer;
    public HypeGraphDrawer hypeGraphDrawer;

    private ShadedProgressBar progressBar;

    public Map map;

    public TextureAtlas bigAtlas;

    private Sprite pauseSprite;

    public Player player;

    private Background background;

    public MapGenerator mapGenerator;
    public final Sprite musicNote;
    public final Sprite blackSprite;
    public final Sprite heartSprite;

    public int notesCollected;

    public InputMultiplexer inputMultiplexer;
    private float lastPf;

    private boolean shouldRegen = false;

    private Stage pauseStage;
    private TextButton menuButton, restartButton;

    private Sound powerdownSound;
    public Sound[] collectSounds;

    public GameScreen(RedMaple redMaple) {
        this.redMaple = redMaple;

        this.inputMultiplexer = new InputMultiplexer();
        this.inputMultiplexer.addProcessor(gd);
        this.inputMultiplexer.addProcessor(this);

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();

        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        font = new BitmapFont();

        shapeRenderer = new ShapeRenderer();

        this.progressBar = new ShadedProgressBar(this);

        this.bigAtlas = new TextureAtlas(Gdx.files.internal("gfx/gfx.pack"));


        this.pauseSprite = bigAtlas.createSprite("paused");
        this.player = new Player(this);
        this.player.create();
        this.player.y = 200;

        this.musicNote = bigAtlas.createSprite("note");
        this.musicNote.setScale(0.25f);

        this.blackSprite = bigAtlas.createSprite("black");
        this.heartSprite = bigAtlas.createSprite("heart");

        this.background = new Background(this);
        this.background.create();


        this.spectrumDrawer = new SpectrumDrawer(this);
        this.spectrumDrawer.init();

        this.fluxGraphDrawer = new FluxGraphDrawer(this);
        this.hypeGraphDrawer = new HypeGraphDrawer(this);

        this.pauseStage = new Stage(camera.viewportWidth, camera.viewportHeight, true, batch);

        this.powerdownSound = Gdx.audio.newSound(Gdx.files.internal("snd/powerdown.ogg"));
        this.collectSounds = new Sound[25];
        for (int i = 12; i < 35; i++) {
            this.collectSounds[i-12] = Gdx.audio.newSound(Gdx.files.internal("snd/piano/a" + i + ".ogg"));
        }

    }

    private float progress;
    private LoadState progressPhase;
    private String fail;

    private ProgressReporter progressReporter = new ProgressReporter() {
        @Override
        public void progress(float percent) {
            progress = percent;
        }

        @Override
        public void finished() {
            progress = 1;
        }

        @Override
        public void failed(String message, Exception e) {
            progress = 0;
            fail = message;
        }
    };

    private void searchOnlineCache() {
        progressPhase = LoadState.SearchingCache;
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                StoredMusic music = getMusic();

                if (redMaple.isCached(music.cachedLowerToString)) {
                    return; // Will be loaded in generating map phase
                }

                try {

                    String params = "?" + URLEncoder.encode("art", "UTF-8") + "=" + URLEncoder.encode(music.artist, "UTF-8") +
                                    "&" + URLEncoder.encode("tit", "UTF-8") + "=" + URLEncoder.encode(music.name, "UTF-8") +
                                    "?" + URLEncoder.encode("fil", "UTF-8") + "=" + URLEncoder.encode(music.fileHandle.nameWithoutExtension(), "UTF-8");

                    URL url = new URL("https://dl.dropbox.com/u/18458187/redmaple/search_win.txt" + params);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setConnectTimeout(2000);

                    JsonReader jr = new JsonReader();

                    ObjectMap<Object, Object> jsonmap = (ObjectMap<Object, Object>) jr.parse(urlConnection.getInputStream());

                    int status = (int) (float) (Float) jsonmap.get("status");

                    if (status == 0) {
                        Gdx.app.log("RedMaple", "Online cachesearch for " + params + ": " + jsonmap);
                        // TODO save the map to local sql
                    }
                    else {
                        Gdx.app.log("RedMaple", "Error while trying to search from onlinecache: " + status + ": " + jsonmap.get("msg"));
                    }

                } catch (IOException e) {
                    Gdx.app.error("RedMaple", "CacheSearch phase failed", e);
                    // We dont have to do anything else because the map is simply regenerated in next phase.
                }
            }
        });
    }

    private void generateMapInThread() {
        //Gdx.app.log("RedMaple", "Generating map ");
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String mapJson = redMaple.getMapJson(getMusic().cachedLowerToString);

                boolean regen = mapJson == null || shouldRegen;

                if (!regen) {
                    Map map = new Map();
                    map.loadFromJson(mapJson);
                    if (map.isVersionSupported()) {
                        Gdx.app.log("RedMaple", "Map loaded from cache");
                        GameScreen.this.map = map;
                    }
                    else {
                        Gdx.app.log("RedMaple", getMusic().cachedLowerToString + " stored version " + map.version + " is not supported. Regenerating the map");
                        regen = true;
                    }
                }

                if (regen) {
                    mapGenerator = new DefaultMapGenerator(GameScreen.this);
                    progressPhase = LoadState.GeneratingMap;
                    mapGenerator.generate(progressReporter);
                    Gdx.app.log("RedMaple", "Map generated");

                    map = mapGenerator.generatedMap();
                    //System.out.println("MapJSON: " + map.createJson());
                    mapGenerator.dispose();

                    audioProcessor.rewind(); // Should set player pos

                    String error = redMaple.saveMap(getMusic().cachedLowerToString, map.createJson());
                    if (error != null)
                        Gdx.app.error("RedMaple", "Failed to save map " + getMusic().cachedLowerToString + ": " + error);

                }

                progressPhase = LoadState.Finished;
                progress = 0;

                GameScreen.this.setPlayerStartPos();

            }
        });
    }

    private void handleLoading() {
        if (threadPool == null || musicPool == null)
            initializeThreadpools();

        if (threadPool.getActiveCount() == 0 && progressPhase != LoadState.Finished) {
            if (progressPhase == null) {
                searchOnlineCache();
            }
            else if (progressPhase == LoadState.SearchingCache) {
                audioProcessor = new AudioProcessor(music.fileHandle);
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            audioProcessor.loadInit(progressReporter);
                            progressPhase = LoadState.Analyzing;
                            if (!redMaple.isCached(music.cachedLowerToString) || shouldRegen)
                                audioProcessor.loadAnalyzer(progressReporter);
                            progress = 0;
                        }
                        catch (Exception e) {
                            Gdx.app.error("RedMaple", "Analyzing phase failed", e);
                            progressPhase = LoadState.SearchingCache; // Redo this step to prevent further damage in generateMap. TODO show error popup
                        }
                    }
                });
            }
            else if (progressPhase == LoadState.Analyzing) {
                generateMapInThread();
            }
        }
    }

    public BitmapFont font;
    public int visibleTick = 0;
    public int visibleSpectrumIndex() {
        return visibleTick / AudioProcessor.SPECTRUM_LENGTH;
    }

    public int stopSlowDown = -1;

    public void triggerSlowDown(int length) {
        if (stopSlowDown == -1) {
            audioProcessor.setVolume(0.5f);
        }
        stopSlowDown = visibleTick + length;
    }


    final float playerLeftOffset = 240;

    float lastCamPosY = 0;

    double deltaMultiplier;

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        handleLoading();

        if (progressPhase == LoadState.Finished && map != null) {

            if (map.textureAtlas == null) {
                map.textureAtlas = new TextureAtlas(Gdx.files.internal("gfx/" + map.atlasPack));
            }

            if (musicPool.getActiveCount() == 0)
                musicPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        audioProcessor.play();
                    }
                });

            double deltaMultiplier = 1;

            if (audioProcessor.getPlayState() == AudioProcessor.PlayState.Playing) {
                float shouldAdd = delta * audioProcessor.samplesPerSecond;

                if (stopSlowDown != -1) {
                    if (stopSlowDown < (visibleTick + shouldAdd)) {
                        stopSlowDown = -1;
                        audioProcessor.setVolume(1);
                    }
                    else {
                        deltaMultiplier = 0.5f;
                    }
                }

                if (stopSlowDown == -1) {
                    float diff = audioProcessor.getLatencyAffectedSampleIndex() - visibleTick;
                    if (diff > 2*AudioProcessor.SPECTRUM_LENGTH) {
                        deltaMultiplier = 1 + ((diff * 0.015f) / shouldAdd); // DURP CODE :D
                        //shouldAdd += diff * 0.1f;
                    }
                }
                visibleTick += shouldAdd * deltaMultiplier;
            }
            else {
                deltaMultiplier = 0;
            }
            this.deltaMultiplier = deltaMultiplier;

            float diff = Math.abs(visibleTick - audioProcessor.getLatencyAffectedSampleIndex());
            if (stopSlowDown == -1 && deltaMultiplier == 1 && diff > AudioProcessor.SPECTRUM_LENGTH*3) {
                //Gdx.app.log("RedMaple", "Doing a ugly hop because diff between vistick and audiotick was too high");
                visibleTick = audioProcessor.getLatencyAffectedSampleIndex();
                //player.y = 300;
            }

            float camPosYShouldBe = camera.viewportHeight / 2 + player.y - 85;

            if (lastCamPosY == 0)
                lastCamPosY = camPosYShouldBe;

            float camYNow = lastCamPosY += ((camPosYShouldBe - lastCamPosY) * 0.04f /* lerp */);

            float deltaMultiplierf = (float) deltaMultiplier;

            if (!isPaused())
                updateWorld(deltaMultiplierf * delta);

            camera.position.set(player.x + playerLeftOffset, camYNow, 0);
            //camera.position.set(camera.viewportWidth / 2 + (visibleSpectrumIndex() / map.spectrumsPerBlock) * map.blockSize - playerLeftOffset, camYNow, 0);
            camera.update();
            batch.setProjectionMatrix(camera.combined);
            shapeRenderer.setProjectionMatrix(camera.combined);

            drawWorld(deltaMultiplierf * delta);
            //audioProcessor.soundTouch.setPitchSemiTones(MathUtils.sin(relativeTime));
        }

        // Draw UI
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawHud();
    }

    public float relativeTime=0;
    public float deathTime = 0;

    public int livesLost = 0;
    public int maxLives = 10;

    private static final float DEATHTIME_LENGTH = 5;
    private static final float DEATHTIME_FADE_LENGTH = DEATHTIME_LENGTH / 10;

    public void updateWorld(float delta) {
        relativeTime += delta;

        if (relativeTime > deathTime)
            deathTime = 0;

        this.player.x = (visibleSpectrumIndex() / map.spectrumsPerBlock) * map.blockSize;
        this.player.update(delta);

        if (player.touchesHorizontally() && deathTime == 0) {
            deathTime = relativeTime + DEATHTIME_LENGTH;
            this.livesLost++;
            this.powerdownSound.play(1.0f);
        }

        // Update trail colors

        float last = trailColorCache[trailColorCache.length-1];
        System.arraycopy(trailColorCache, 0, trailColorCache, 1, trailColorCache.length-1);
        trailColorCache[0] = last;

        if (deathTime != 0) {

            if (deathTime-relativeTime > (DEATHTIME_LENGTH-DEATHTIME_FADE_LENGTH)) {
                float mul = ((deathTime-relativeTime) - (DEATHTIME_LENGTH-DEATHTIME_FADE_LENGTH)) / DEATHTIME_FADE_LENGTH; // wat
                mul *= 0.5f;
                mul += 0.5f; // clamp to 0.5-1

                audioProcessor.setVolume(mul);
            }
            else if (deathTime-relativeTime < DEATHTIME_FADE_LENGTH) {
                float mul = 1 - (deathTime-relativeTime) / DEATHTIME_FADE_LENGTH;
                mul *= 0.5f;
                mul += 0.5f; // clamp to 0.5-1

                audioProcessor.setVolume(mul);
            }

        }

    }

    Sprite grassSprite;

    public float time;

    private static final int MAX_NOTE_TRAILS = 15;
    private float[] trailYCache = new float[MAX_NOTE_TRAILS];
    private float[] trailColorCache;
    {
        trailColorCache = ColorUtils.createArray(MAX_NOTE_TRAILS*20);
    }

    public void pushTrail(float color) {
        //System.arraycopy(trailColorCache, 0, trailColorCache, 1, trailColorCache.length-1);
        //trailColorCache[0] = color;
    }

    private void drawWorld(float delta) {

        background.draw();

        //shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float visibleMin = camera.position.x - camera.viewportWidth/2;
        float visibleMax = camera.position.x + camera.viewportWidth/2;

        /*
        if (grassSprite == null) {
            grassSprite = map.textureAtlas.createSprite("grass");
            grassSprite.setScale(0.2f);
        }
        */

        batch.begin();
        batch.enableBlending();

        for (Block b : map.blocks) {
            float x = b.x * map.blockSize;
            float width = map.blockSize*b.tx;
            if (x < visibleMin && width < visibleMin)
                continue;
            if (x > visibleMax)
                continue;
            if (!b.toggleState)
                continue;

            b.draw(this, batch, map.blockSize);

            //System.out.println(rotation);

            //shapeRenderer.rect(x, b.y, width, map.blockSize);

            /*
            float shouldMin = grassSprite.getHeight() * grassSprite.getScaleY() * 2;
            for (int i = 0;i < b.tx - b.x - 1; i+= 3) {
                grassSprite.setPosition(x - grassSprite.getWidth()/2 + i*map.blockSize, b.y * map.blockSize + b.height * map.blockSize - shouldMin);
                grassSprite.draw(batch);
            }
            */

        }

        if (map.spriteBlocks != null) {
            for (SpriteBlock sp : map.spriteBlocks) {
                float x = sp.x * map.blockSize;
                if (x > visibleMax)
                    continue;

                if (sp.sprite == null) {
                    sp.sprite = map.textureAtlas.createSprite(sp.id);

                    sp.sprite.setX(x);
                    sp.sprite.setY(sp.y * map.blockSize + sp.sprite.getHeight() - 20);
                    sp.sprite.setColor(1, 1, 1, sp.alpha);
                    sp.sprite.setScale(sp.scale);
                }
                sp.sprite.setY(sp.y * map.blockSize + sp.sprite.getHeight()*sp.sprite.getScaleY() - 20);

                sp.sprite.draw(batch);

            }
        }

        batch.disableBlending();

        batch.end();

        //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        //shapeRenderer.line();
        //shapeRenderer.end();

        //shapeRenderer.end();

        this.player.render();

        Sprite note = this.musicNote;

        batch.begin();
        batch.enableBlending();

        float scale = 0.1f;

        float trx = -(note.getWidth() - note.getWidth() * scale) / 2;
        float trz = -(note.getHeight() - note.getHeight() * scale) / 2;

        int nmax = Math.min(MAX_NOTE_TRAILS, notesCollected);

        trailYCache[0] = this.player.y + this.player.bb.height/2;
        for (int i = nmax-1; i > 0; i--) {
            float diff = trailYCache[i-1] - trailYCache[i];
            float mul = Math.min(delta * 20, 1);
            trailYCache[i] += diff * mul;
        }

        boolean isCombo = getCombo() != 0;

        for (int i = 0; i < nmax; i++) {

            float basey = trailYCache[i];

            float x = this.player.x - 40 - i*13;
            float y = basey + MathUtils.sin(time + i*0.3f)*3;

            x += trx;
            y += trz;

            note.setColor(Color.BLACK);

            if (isCombo) {
                float oldscale = note.getScaleX();

                note.setScale(oldscale * 1.1f);
                note.setPosition(x, y);
                note.draw(batch);

                note.setScale(oldscale);
                note.setColor(trailColorCache[i]);
            }

            note.setPosition(x, y);
            note.draw(batch);

        }

        time += delta * (nmax);
        time %= 100;

        batch.end();
        batch.disableBlending();


    }

    public void reset() {


        musicPool.shutdownNow();
        threadPool.shutdownNow();

        musicPool = threadPool = null;

        audioProcessor.dispose();
        audioProcessor = null;
        map = null;
        mapGenerator = null;
        deathTime = 0;
        notesCollected = 0;
        progress = 0;
        progressPhase = null;
        time = 0;
        relativeTime = 0;
        livesLost = 0;

        System.gc();
    }

    public void gameReset() {
        audioProcessor.rewind();
        visibleTick = 0;
        time = 0;
        relativeTime = 0;
        livesLost = 0;
        deathTime = 0;
        notesCollected = 0;

        for (Block b : map.blocks) {
            if (b instanceof NoteBlock)
                b.toggleState = true;
        }
    }

    public void setPlayerStartPos() {
        player.y = map.startY*map.blockSize + 200;
        lastCamPosY = player.y;
    }

    private void drawHud() {

        final float width = camera.viewportWidth;
        final float height = camera.viewportHeight;

        if (progressPhase != LoadState.Finished) {

            progressBar.draw(progressPhase, progress);

            return;
        }

        if (GameSettings.DebugGfx) {
            this.spectrumDrawer.draw();
            this.hypeGraphDrawer.draw();
            this.fluxGraphDrawer.draw();
        }

        batch.begin();
        batch.enableBlending();

        float y = 10;

        if (GameSettings.DebugGfx) {

            font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, y += 15);
            if (mapGenerator != null)
                font.draw(batch, "Energy: " + mapGenerator.getEnergy(audioProcessor.getLatencyAffectedSpectrumIndex()), 10, y += 15);

            /*
                            "; Flux: " + audioProcessor.computeFlux(audioProcessor.getLatencyAffectedSpectrumIndex()) +
                            "; Threshold: " + audioProcessor.computeThresholdMean(audioProcessor.getLatencyAffectedSpectrumIndex(), 10) +
                            "; PlayState: " + audioProcessor.getPlayState() +
                            "; SampleIndex: " + audioProcessor.getSampleIndex() + " vs " + visibleTick
             */
            if (audioProcessor.hasFluxData()) {
                font.draw(batch, "Flux: " + audioProcessor.computeFlux(audioProcessor.getLatencyAffectedSpectrumIndex()), 10, y += 15);

                float pf = audioProcessor.computePrunnedFlux(audioProcessor.getLatencyAffectedSpectrumIndex());
                if (pf != 0)
                    lastPf = pf;

                font.draw(batch, "LastPrunedFlux: " + lastPf, 10, y += 15);
                font.draw(batch, "Threshold: " + audioProcessor.computeThresholdMean(audioProcessor.getLatencyAffectedSpectrumIndex(), 10), 10, y += 15);
            }
            font.draw(batch, "Playstate: " + audioProcessor.getPlayState(), 10, y += 15);
            font.draw(batch, "SampleIndex: " + audioProcessor.getSampleIndex(), 10, y += 15);
            font.draw(batch, "VisibleTickIndex: " + visibleTick, 10, y += 15);
            font.draw(batch, "DeltaMultiplier: " + deltaMultiplier, 10, y += 15);
            font.draw(batch, "SpectrumIndex: " + audioProcessor.getSpectrumIndex(), 10, y += 15);
            font.draw(batch, "(LatencyMod) SpectrumIndex: " + audioProcessor.getLatencyAffectedSpectrumIndex(), 10, y += 15);
            if (mapGenerator != null)
                font.draw(batch, "Peaks within 500: " + mapGenerator.countPeaks(audioProcessor.getLatencyAffectedSpectrumIndex(), 50), 10, y += 15);
            String s = "";
            for (float i = -3f;i < 4; i+= 1f) {
                s += i+ ": " + player.touchesBlock(0, i).size + "; ";
            }
            font.draw(batch, "TG's: " + s, 10, y += 15);

            font.draw(batch, "horiz: " + player.touchesGround(0, 3) + " v " + player.touchesGround(-player.bb.width, 3), 10, y += 15);

            s = "";
            for (float f : trailYCache)
                s += MathUtils.round(f) + ", ";
            font.draw(batch, "trail bases: " + s, 10, y += 15);
            font.draw(batch, "player coords: " + MathUtils.round(player.x) + "x" +MathUtils.round( player.y), 10, y += 15);
            font.draw(batch, "REL: " + relativeTime, 10, y += 15);
            font.draw(batch, "DT: " + deathTime, 10, y += 15);
            font.draw(batch, "volume: " + audioProcessor.getVolume(), 10, y += 15);
            font.draw(batch, "combo: " + getCombo() + " z: " + comboz, 10, y += 15);

        }

        if (isPaused()) {
            batch.draw(pauseSprite, camera.viewportWidth/2- pauseSprite.getWidth()/2, camera.viewportHeight/2- pauseSprite.getHeight()/2);
        }

        {
            int lives = maxLives - livesLost;
            for (int l = 0;l < lives; l++) {
                heartSprite.setPosition(10 + l*35, 10);
                heartSprite.draw(batch);
            }
        }

        batch.disableBlending();
        batch.end();

        if (isPaused()) {
            if (this.menuButton == null) {
                this.menuButton = new TextButton("Main menu", redMaple.menuScreen.menuUi.skin);
                this.menuButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        GameScreen.this.reset();
                        redMaple.switchTo(RedMaple.GScreen.Menu);
                    }
                });

                float vw = camera.viewportWidth;
                float vh = camera.viewportHeight;

                float partX = vw / 10, partY = vh / 10;
                float sw = vw - partX*2;

                this.menuButton.setPosition(partX, partY);
                this.menuButton.setSize(sw / 2, vh/4);

                pauseStage.addActor(this.menuButton);
            }
            if (this.restartButton == null) {
                this.restartButton = new TextButton("Restart", redMaple.menuScreen.menuUi.skin);
                this.restartButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        GameScreen.this.gameReset();
                        setPlayerStartPos();
                        setPaused(false);
                    }
                });

                float vw = camera.viewportWidth;
                float vh = camera.viewportHeight;

                float partX = vw / 10, partY = vh / 10;
                float sw = vw - partX*2;

                this.restartButton.setPosition(partX + sw/2, partY);
                this.restartButton.setSize(sw / 2, vh/4);

                pauseStage.addActor(this.restartButton);
            }

            this.pauseStage.act(Gdx.graphics.getDeltaTime());

            batch.enableBlending();
            this.pauseStage.draw();
            batch.disableBlending();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCatchBackKey(false);
    }

    public boolean isPaused() {
        return audioProcessor != null && audioProcessor.getPlayState() == AudioProcessor.PlayState.Paused;
    }

    public void setPaused(boolean b) {
        if (b)
            pause();
        else
            resume();
    }

    @Override
    public void pause() {
        audioProcessor.setPlayState(AudioProcessor.PlayState.Paused);
        if (!inputMultiplexer.getProcessors().contains(this.pauseStage, true)) {
            inputMultiplexer.addProcessor(this.pauseStage);
        }
        System.out.println("Processors: " + inputMultiplexer.getProcessors());
    }

    @Override
    public void resume() {
        audioProcessor.setPlayState(AudioProcessor.PlayState.Playing);
        inputMultiplexer.removeProcessor(this.pauseStage);
    }

    @Override
    public void dispose() {
        if (audioProcessor != null)
            audioProcessor.dispose();
        if (spectrumDrawer != null)
            spectrumDrawer.dispose();

        batch.dispose();
        shapeRenderer.dispose();

        if (threadPool != null)
            threadPool.shutdownNow();

        if (musicPool != null)
            musicPool.shutdownNow();

        this.powerdownSound.dispose();
        for (Sound snd : this.collectSounds)
            snd.dispose();

        Gdx.app.log("RedMaple", "Disposing GameScreen");
    }

    public void setMusic(StoredMusic music, boolean b) {
        this.music = music;
        this.audioProcessor = null;

        this.shouldRegen = b;
    }

    public StoredMusic getMusic() {
        return music;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.P || keycode == Input.Keys.BACK) {
            setPaused(!isPaused());
            return true;
        }
        else if (keycode == Input.Keys.SPACE || keycode == Input.Keys.UP) {
            player.jump();
            return true;
        }
        else if (keycode == Input.Keys.DOWN) {
            player.slide(0);
            return true;
        }
        if (GameSettings.Debug) {
            if (keycode == Input.Keys.ENTER) {
                Gdx.app.log("RedMaple", "Reloading Map");
                generateMapInThread();
                return true;
            }
            else if (keycode == Input.Keys.RIGHT) {
                audioProcessor.skipSamples((int) (audioProcessor.samplesPerSecond * 5));
                return true;
            }
            else if (keycode == Input.Keys.LEFT) {
                audioProcessor.skipSamples((int) -(audioProcessor.samplesPerSecond * 5));
                return true;
            }
            else if (keycode == Input.Keys.D) {
                GameSettings.DebugGfx = !GameSettings.DebugGfx;
                return true;
            }
            else if (keycode == Input.Keys.S) {
                background.reloadShader();
                return true;
            }
            else if (keycode == Input.Keys.Y) {
                triggerSlowDown((int) (audioProcessor.samplesPerSecond * 0.5f));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.DOWN) {
            player.endSlide();
            return true;
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean keyTyped(char character) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean scrolled(int amount) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    GestureDetector gd = new GestureDetector(new GestureDetector.GestureListener() {
        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            if (!isPaused()) { // This prevents down fling but yolo for now
                if (map != null)
                    player.jump();
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            if (!isPaused()) {
                if (map != null)
                    player.jump();
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            if (isPaused())
                return false;
            if (Math.abs(velocityX) > Math.abs(velocityY))
                return false;

            if (velocityY < 0) {
                if (map != null)
                    player.jump();
            }
            else {
                if (map != null)
                    player.slide(0.5f);
                // crouch
            }
            Gdx.app.log("RedMaple", "Flinged " + velocityX + " " + velocityY);
            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    });

    public boolean isDead() {
        return deathTime != 0;
    }

    private int comboz = 0;
    private long comboTiem = 0;

    public float getCombo() {
        return Math.max(comboTiem - TimeUtils.millis(), 0);
    }

    public void popNote(NoteBlock nb) {

        notesCollected++;
        pushTrail(0);

        if (getCombo() == 0) {
            collectSounds[comboz].play(0.2f);
            if (comboz++ > 21) {
                comboz = 0;
                comboTiem = TimeUtils.millis() + 7000;
            }
        }
        else {

        }
    }

    public enum LoadState {
        SearchingCache,
        Analyzing,
        GeneratingMap,
        Finished
    }

}
