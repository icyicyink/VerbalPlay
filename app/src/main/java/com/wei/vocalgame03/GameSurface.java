package com.wei.vocalgame03;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private final SpeechRecognizer speechRecognizer;
    private PlayerCharacter player;
    private final List<Enemy> enemyList = new ArrayList<>();
    private final List<Enemy> potentialEnemyList = new ArrayList<>();
    private Bitmap effectBitmapFireball;
    private Bitmap effectBitmapLightning;
    private final List<Effect> effects = new ArrayList<>();

    private static final int MAX_STREAMS = 100;
    private final ImageButton actionButton;
    private final EditText editText;

    private int fadeAlpha = 255;
    private boolean instructionsDone = false;
    private int instructionsIndex = 0;
    private long lastInstructionTime = 0;
    private long levelStartTime = 0;
    private long timeSinceLevelStart = 0;

    // Todo refactor all this text
    private final String[] instLv1 = {
            "You find yourself in a dark forest.",
            "Hold the wand and it will listen.",
            "Release to cast.",
            "Say 'FIRE' to cast fireball!",
            "They are coming..."
    };

    private final String[] instLv2 = {
            "You see faster and more dangerous enemies between the trees.",
            "Try 'LIGHTNING' to strike down taller foes.",
            "They are coming..."
    };

    private final String[] instLv3 = {
            "As you make camp for the night,",
            "A friendly faerie shares your campfire.",
            "They have a riddle for you."
    };

    private final String[] instLv4 = {
            "You see a massive wave of enemies approaching.",
            "They are coming..."
    };

    private final String[] instLv5 = {
            "As you stare into the campfire,",
            "A mischievous voice comes from the flames.",
            "Another riddle it seems"
    };

    private final String[] instLv6 = {
            "You feel a terrible foe approaching.",
            "Be ready..."
    };

    private final String[] puzzle0 = {
            "What has two hands on its face but no arms?"
    };

    private final String[] puzzle1 = {
            "What has a head and a tail but no body?"
    };

    private final String puzzle0Answer = "Clock";
    private final String puzzle1Answer = "Coin";

    private String[] instructionsCurrent = instLv1;
    private final Paint instructionPaint;

    private boolean isDefeated = false;
    private int defeatFadeAlpha = 0;
    private final Paint defeatPaint;

    // Level system
    private int currentLevel = 1;
    private static final int MAX_LEVELS = 6;
    private boolean levelComplete = false;
    private int levelCompleteFadeAlpha = 0;
    private boolean showLevelPrompt = false;
    private long levelCompleteTime = 0;
    private final Paint levelPaint;

    // Puzzle system
    private boolean isPuzzleLevel = false;
    private String[] currentPuzzle = null;
    private String currentPuzzleAnswer = null;
    private final Paint puzzlePaint;

    // Background stuff
    private Bitmap[] bgLayers = new Bitmap[5];
    private float[] bgOffsets = new float[5];
    private float[] bgSpeeds = {3f, 2f, 1f, 0.5f, 0.2f};

    private BackgroundObject campfire;

    AudioManagerCustom audioManager;

    public GameSurface(Context context, ImageButton btn, EditText editText, int startLevel) {
        super(context);

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        audioManager = AudioManagerCustom.get(context);

        currentLevel = startLevel;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getContext());

        this.actionButton = btn;

        this.editText = editText;

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String result = data.get(0);
                    editText.setText(result);

                    // Handle puzzle level answer
                    if (isPuzzleLevel && currentPuzzleAnswer != null) {
                        if (result.equalsIgnoreCase(currentPuzzleAnswer)) {
                            levelComplete = true;
                            //Todo switch with level complete sound
                        }
                        return;
                    }

                    if (result.equalsIgnoreCase("fire")) {
                        Enemy closestEnemy = null;
                        int minX = Integer.MAX_VALUE;

                        for (Enemy enemy : enemyList) {
                            if (enemy.getHealth() > 0 && enemy.getX() < minX) {
                                minX = (int) enemy.getX();
                                closestEnemy = enemy;
                            }
                        }

                        if (closestEnemy != null) {
                            closestEnemy.takeDamage(50);
                            audioManager.playFireball();
                            // Spawn hit effect centered on the enemy
                            spawnEffectAtEnemy(closestEnemy, Spells.FIREBALL);
                        }
                    }
                    else if (result.equalsIgnoreCase("lightning")) {
                        if (currentLevel > 1) {
                            Enemy tallestEnemy = findTallestEnemy();
                            if (tallestEnemy != null) {
                                tallestEnemy.takeDamage(50);
                                audioManager.playLightning();
                                spawnEffectAtEnemy(tallestEnemy, Spells.LIGHTNING);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
        actionButton.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!instructionsDone || isDefeated || levelComplete) return false;

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    speechRecognizer.stopListening();
                    actionButton.setImageResource(R.drawable.wand);
                    System.out.println("ACTION_UP");
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    actionButton.setImageResource(R.drawable.wand1);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    System.out.println("ACTION_DOWN");
                    return true;
                }
                return false;
            }
        });

        instructionPaint = new Paint();
        instructionPaint.setColor(Color.WHITE);
        instructionPaint.setTextSize(60);
        instructionPaint.setTextAlign(Paint.Align.CENTER);
        instructionPaint.setFakeBoldText(true);
        instructionPaint.setShadowLayer(5, 0, 0, Color.BLACK);

        defeatPaint = new Paint();
        defeatPaint.setColor(Color.RED);
        defeatPaint.setTextSize(100);
        defeatPaint.setTextAlign(Paint.Align.CENTER);
        defeatPaint.setFakeBoldText(true);
        defeatPaint.setShadowLayer(10, 0, 0, Color.BLACK);

        levelPaint = new Paint();
        levelPaint.setColor(Color.WHITE);
        levelPaint.setTextSize(80);
        levelPaint.setTextAlign(Paint.Align.CENTER);
        levelPaint.setFakeBoldText(true);
        levelPaint.setShadowLayer(5, 0, 0, Color.BLACK);

        puzzlePaint = new Paint();
        puzzlePaint.setColor(Color.WHITE);
        puzzlePaint.setTextSize(50);
        puzzlePaint.setTextAlign(Paint.Align.CENTER);
        puzzlePaint.setFakeBoldText(true);
        puzzlePaint.setShadowLayer(5, 0, 0, Color.BLACK);
    }

    private Enemy findTallestEnemy() {
        Enemy tallestEnemy = null;
        int maxHeight = 0;
        float minX = Float.MAX_VALUE;

        for (Enemy enemy : enemyList) {
            if (enemy.getHealth() > 0) {
                int enemyHeight = enemy.getLightningPriority();
                if (enemyHeight > maxHeight) {
                    maxHeight = enemyHeight;
                    tallestEnemy = enemy;
                    minX = enemy.getX();
                }
                else if (enemyHeight == maxHeight && enemy.getX() < minX) {
                    tallestEnemy = enemy;
                    minX = enemy.getX();
                }
            }
        }
        return tallestEnemy;
    }

    private void spawnEffectAtEnemy(Enemy enemy, Spells spell) {
        int effectColsFire = 13;
        int effectColsLightning = 7;
        int effectCols = 0;

        Bitmap effectBitmap = null;
        switch (spell) {
            case FIREBALL:
                effectBitmap = this.effectBitmapFireball;
                effectCols = effectColsFire;
                break;
            case LIGHTNING:
                effectBitmap = this.effectBitmapLightning;
                effectCols = effectColsLightning;
        }
        if (effectBitmap == null) return;

        float ex = enemy.getX();
        float ey = enemy.getY();
        Effect ef = new Effect(effectBitmap, 1, effectCols, ex, ey, 1f);
        this.effects.add(ef);
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public void update() {
        if (isDefeated) {
            if (defeatFadeAlpha < 255) {
                defeatFadeAlpha += 5;
                if (defeatFadeAlpha > 255) defeatFadeAlpha = 255;
            }
            return;
        }

        // Handle level completion
        if (levelComplete) {
            if (!showLevelPrompt) {
                // Fade to black
                if (levelCompleteFadeAlpha < 255) {
                    levelCompleteFadeAlpha += 5;
                    if (levelCompleteFadeAlpha >= 255) {
                        levelCompleteFadeAlpha = 255;
                        showLevelPrompt = true;
                        levelCompleteTime = System.currentTimeMillis();
                    }
                }
            }
            return;
        }

        if (!instructionsDone) {
             if (lastInstructionTime == 0) lastInstructionTime = System.currentTimeMillis();
             if (System.currentTimeMillis() - lastInstructionTime > 3000) {
                 instructionsIndex++;
                 lastInstructionTime = System.currentTimeMillis();
                 if (instructionsIndex >= instructionsCurrent.length) {
                     instructionsDone = true;
                 }
             }
             return;
         } else {
             if (levelStartTime == 0) {
                 levelStartTime = System.currentTimeMillis();
             } else {
                 timeSinceLevelStart = System.currentTimeMillis() - levelStartTime;
             }

             if (!isPuzzleLevel) {
                 // Only spawn enemy if their spawn timer is up
                 Iterator<Enemy> spawnIterator = potentialEnemyList.iterator();
                 while (spawnIterator.hasNext()) {
                     Enemy enemy = spawnIterator.next();
                     if (enemy.getSpawnTime() <= timeSinceLevelStart) {
                         enemyList.add(enemy);
                         spawnIterator.remove();
                     }
                 }
             }
         }

        // Update parallax offsets
        for (int i = 0; i < 5; i++) {
            bgOffsets[i] -= bgSpeeds[i];
            if (bgLayers[i] != null && bgOffsets[i] <= -bgLayers[i].getWidth()) {
                bgOffsets[i] += bgLayers[i].getWidth();
            }
        }

        if (fadeAlpha > 0) {
            fadeAlpha -= 5;
            if (fadeAlpha < 0) fadeAlpha = 0;
            return;
        }

        if (player != null) {
            player.update();
            if (player.getHealth() <= 0) {
                isDefeated = true;
            }
        }

        if (isPuzzleLevel && campfire != null) {
            campfire.update();
        }

        Iterator<Enemy> iterator = enemyList.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.update();
            if (enemy.getHealth() <= 0) {
                iterator.remove();
            }
        }
        // Update active effects and remove finished ones
         Iterator<Effect> eit = effects.iterator();
         while (eit.hasNext()) {
             Effect ef = eit.next();
             ef.update();
             if (ef.isFinished()) {
                 eit.remove();
             }
         }

         if (!levelComplete) {
             if (!isPuzzleLevel) {
                 if (potentialEnemyList.isEmpty() && enemyList.isEmpty()) {
                     levelComplete = true;
                 }
             }
         }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawColor(Color.BLACK);

        if (instructionsDone) {
             if (!isPuzzleLevel) {
                 // Draw background
                 for (int i = 4; i >= 0; i--) {
                     if (bgLayers[i] != null) {
                         canvas.drawBitmap(bgLayers[i], bgOffsets[i], 0, null);
                         canvas.drawBitmap(bgLayers[i], bgOffsets[i] + bgLayers[i].getWidth(), 0, null);
                     }
                 }
                 if (player != null) player.draw(canvas);
                 for (Enemy enemy : enemyList) {
                     enemy.draw(canvas);
                 }
             } else {
                 campfire.draw(canvas);
                 drawPuzzle(canvas);
             }

             // Fade in
             if (fadeAlpha > 0) {
                 canvas.drawARGB(fadeAlpha, 0, 0, 0);
             }

             if (player != null && !isDefeated && !levelComplete) {
                 drawHPBar(canvas);
                 drawLevelDisplay(canvas);
             }
         }

        // Draw effects
        for (Effect ef : effects) {
            ef.draw(canvas);
        }

        switch (currentLevel) {
            case 1:
                instructionsCurrent = instLv1;
                break;
            case 2:
                // Setup puzzle instructions before showing next level
                instructionsCurrent = instLv2;
                break;
            case 3:
                instructionsCurrent = instLv3;
                break;
            case 4:
                instructionsCurrent = instLv4;
                break;
            case 5:
                instructionsCurrent = instLv5;
                break;
            case 6:
                instructionsCurrent = instLv6;
                break;
        }

        // Draw instructions
        if (!instructionsDone && instructionsIndex < instructionsCurrent.length) {
            canvas.drawText(instructionsCurrent[instructionsIndex], getWidth() / 2f, getHeight() / 2f, instructionPaint);
        }

         // Draw level complete prompt
         if (levelComplete) {
             effects.clear();
             canvas.drawARGB(levelCompleteFadeAlpha, 0, 0, 0);
             if (showLevelPrompt && levelCompleteFadeAlpha >= 255) {
                 String levelText;
                 if (currentLevel >= MAX_LEVELS) {
                     levelText = "VICTORY!";
                 } else {
                     levelText = "Level " + currentLevel + " Complete!";
                 }
                 canvas.drawText(levelText, getWidth() / 2f, getHeight() / 2f - 100, levelPaint);

                 instructionsDone = false;
                 instructionsIndex = 0;
                 lastInstructionTime = 0;
                 canvas.drawText("Tap to Continue", getWidth() / 2f, getHeight() / 2f + 100, instructionPaint);
             }
         }

        if (isDefeated) {
            canvas.drawARGB(defeatFadeAlpha, 0, 0, 0);
            if (defeatFadeAlpha >= 255) {
                canvas.drawText("YOU WERE DEFEATED", getWidth() / 2f, getHeight() / 2f, defeatPaint);
                canvas.drawText("Tap to Restart", getWidth() / 2f, getHeight() / 2f + 120, instructionPaint);
            }
        }
    }

    private void drawLevelDisplay(Canvas canvas) {
         Paint levelDisplayPaint = new Paint();
         levelDisplayPaint.setColor(Color.WHITE);
         levelDisplayPaint.setTextSize(60);
         levelDisplayPaint.setTextAlign(Paint.Align.RIGHT);
         levelDisplayPaint.setFakeBoldText(true);
         levelDisplayPaint.setShadowLayer(5, 0, 0, Color.BLACK);
         canvas.drawText("Level: " + currentLevel, getWidth() - 50, 120, levelDisplayPaint);
     }

    private void drawPuzzle(Canvas canvas) {
        if (currentPuzzle == null) return;

        int startY = getHeight() / 4;
        int lineSpacing = 100;

        for (int i = 0; i < currentPuzzle.length; i++) {
            canvas.drawText(currentPuzzle[i], getWidth() / 2f, startY + (i * lineSpacing), puzzlePaint);
        }

        // Instruct user to use wand to answer puzzle because it's apparently not obvious
        Paint hintPaint = new Paint();
        hintPaint.setColor(Color.YELLOW);
        hintPaint.setTextSize(40);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setFakeBoldText(true);
        hintPaint.setShadowLayer(3, 0, 0, Color.BLACK);
        canvas.drawText("Hold the wand and speak your answer", getWidth() / 2f, getHeight() - 150, hintPaint);
    }

    private void drawHPBar(Canvas canvas) {
        int health = player.getHealth();
        if (health < 0) health = 0;
        int maxHealth = 100;
        int barWidth = 600;
        int barHeight = 60;
        int barLeft = (getWidth() - barWidth) / 2;
        int barTop = 150;

        Paint barPaint = new Paint();
        // Background
        barPaint.setColor(Color.GRAY);
        canvas.drawRect(barLeft, barTop, barLeft + barWidth, barTop + barHeight, barPaint);

        // Foreground (HP)
        if (health > 50) {
            barPaint.setColor(Color.GREEN);
        } else if (health > 20) {
            barPaint.setColor(Color.YELLOW);
        } else {
            barPaint.setColor(Color.RED);
        }
        canvas.drawRect(barLeft, barTop, barLeft + (barWidth * health / maxHealth), barTop + barHeight, barPaint);

        // Border
        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setStrokeWidth(5);
        barPaint.setColor(Color.BLACK);
        canvas.drawRect(barLeft, barTop, barLeft + barWidth, barTop + barHeight, barPaint);

        // Text
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(Color.WHITE);
        barPaint.setTextSize(40);
        barPaint.setTextAlign(Paint.Align.CENTER);
        barPaint.setFakeBoldText(true);
        canvas.drawText("HP: " + health + " / " + maxHealth, getWidth() / 2f, barTop + barHeight - 15, barPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDefeated && defeatFadeAlpha >= 255) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                performClick();
                resetGame();
                return true;
            }
        }
        if (levelComplete && showLevelPrompt && levelCompleteFadeAlpha >= 255) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                performClick();
                proceedToNextLevel();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void resetGame() {
         isDefeated = false;
         defeatFadeAlpha = 0;
         fadeAlpha = 255;
         instructionsDone = false;
         instructionsIndex = 0;
         lastInstructionTime = 0;
         levelStartTime = 0;
         timeSinceLevelStart = 0;
         currentLevel = 1;
         levelComplete = false;
         levelCompleteFadeAlpha = 0;
         showLevelPrompt = false;
         levelCompleteTime = 0;
         instructionsCurrent = instLv1;
         isPuzzleLevel = false;
         currentPuzzle = null;
         currentPuzzleAnswer = null;
         audioManager.release();

         initEntities();
     }

    private void proceedToNextLevel() {
         if (currentLevel >= MAX_LEVELS) {
             resetGame();
             return;
         }

         currentLevel++;
         levelComplete = false;
         levelCompleteFadeAlpha = 0;
         showLevelPrompt = false;
         levelCompleteTime = 0;
         fadeAlpha = 255;
         levelStartTime = 0;
         timeSinceLevelStart = 0;
         isPuzzleLevel = false;
         currentPuzzle = null;
         currentPuzzleAnswer = null;

         initEntities();
     }

    private void initEntities() {
        int enemyX = 1400;
        Bitmap playerBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.player);
        Bitmap campfireBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.campfire);


        int viewHeight = getHeight();
        int viewWidth = getWidth();

        float playerScaleFactor = 2f;
        float campfireScaleFactor = 5f;
        float slimeScaleFactor = playerScaleFactor;
        float mushroomScaleFactor = playerScaleFactor * 2;
        float bossScaleFactor = playerScaleFactor * 3;

        campfire  = new BackgroundObject(campfireBitmap, 1, 6, (float) (viewWidth * 0.5), (float) (viewHeight * 0.5),6, campfireScaleFactor);
        // Ground = 90% down screen
        int groundY = (int) (viewHeight * 0.9);

        int playerY = groundY - 50;

        player = new PlayerCharacter(this, playerBitmap, 100, playerY, playerScaleFactor);

        enemyList.clear();
        potentialEnemyList.clear();

        switch (currentLevel) {
            case 1:
                audioManager.playBgm(R.raw.forestbackground);
                Enemy slime1 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                slime1.setSpawnTime(0);
                potentialEnemyList.add(slime1);
                break;
            case 2:
                audioManager.playBgm(R.raw.forestbackground);
                Enemy slime2 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                Enemy slime3 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                slime2.setSpawnTime(0);
                slime3.setSpawnTime(5000);
                Enemy mushroom1 = EnemyFactory.createMushroom(this, enemyX, groundY, mushroomScaleFactor);
                mushroom1.setSpawnTime(6000);
                potentialEnemyList.add(slime2);
                potentialEnemyList.add(slime3);
                potentialEnemyList.add(mushroom1);
                break;
            case 3:
                // Puzzle Level
                audioManager.playBgm(R.raw.campfire);
                isPuzzleLevel = true;
                currentPuzzle = puzzle0;
                currentPuzzleAnswer = puzzle0Answer;
                break;
            case 4:
                audioManager.playBgm(R.raw.forestbackground);
                isPuzzleLevel = false;
                Enemy slime4 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                slime4.setSpawnTime(0);
                Enemy slime5 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                slime5.setSpawnTime(6000);
                Enemy slime6 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                slime6.setSpawnTime(10000);
                Enemy slime7 = EnemyFactory.createForestSlime(this, enemyX, groundY, slimeScaleFactor);
                slime7.setSpawnTime(18000);
                Enemy mushroom2 = EnemyFactory.createMushroom(this, enemyX, groundY, mushroomScaleFactor);
                mushroom2.setSpawnTime(14000);
                Enemy mushroom3 = EnemyFactory.createMushroom(this, enemyX, groundY, mushroomScaleFactor);
                mushroom3.setSpawnTime(22000);
                potentialEnemyList.add(slime4);
                potentialEnemyList.add(slime5);
                potentialEnemyList.add(slime6);
                potentialEnemyList.add(slime7);
                potentialEnemyList.add(mushroom2);
                potentialEnemyList.add(mushroom3);
                break;
            case 5:
                audioManager.playBgm(R.raw.campfire);
                isPuzzleLevel = true;
                currentPuzzle = puzzle1;
                currentPuzzleAnswer = puzzle1Answer;
                break;
            case 6:
                audioManager.playBgm(R.raw.bossmusic);
                isPuzzleLevel = false;
                Enemy boss = EnemyFactory.createBossSlime(this, enemyX, groundY, bossScaleFactor);
                potentialEnemyList.add(boss);
        }

        // Load effect bitmaps
        effectBitmapFireball = BitmapFactory.decodeResource(this.getResources(), R.drawable.effectfiresmall);
        effectBitmapLightning = BitmapFactory.decodeResource(this.getResources(), R.drawable.effectlightningsmall);
    }

    private void initBackground() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int[] resIds = {
                R.drawable.bglayer1,
                R.drawable.bglayer2,
                R.drawable.bglayer3,
                R.drawable.bglayer4,
                R.drawable.bglayer5
        };

        for (int i = 0; i < 5; i++) {
            Bitmap raw = BitmapFactory.decodeResource(getResources(), resIds[i]);
            if (raw != null) {
                bgLayers[i] = Bitmap.createScaledBitmap(raw, width, height, true);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initBackground();
        initEntities();

        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initBackground();
        initEntities();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                if (this.gameThread != null) {
                    this.gameThread.setRunning(false);
                    this.gameThread.join();
                }
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
