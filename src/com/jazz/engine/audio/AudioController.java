package com.jazz.engine.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/**
 * Utility class for playing audio files.
 */
public class AudioController {
    private Clip moveClip;
    private Clip captureClip;
    private Clip checkClip;
    private Clip checkmateClip;
    private Clip newgameClip;

    private static final String BASE = "assets/sounds/2.0/";
    private static final String MOVE_SOUND_PATH = BASE+"move.wav";
    private static final String CAPTURE_SOUND_PATH = BASE+"capture.wav";
    private static final String CHECK_SOUND_PATH = BASE+"check.wav";
    private static final String CHECKMATE_SOUND_PATH = BASE+"checkmate.wav";
    private static final String NEWGAME_SOUND_PATH = BASE+"newgame.wav";

    private static AudioController instance;
    public static AudioController getInstance(){
        return instance;
    }
    public static AudioController create(){
        instance = new AudioController();
        return getInstance();
    }

    public AudioController() {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            URL moveURL = classLoader.getResource(MOVE_SOUND_PATH);
            URL captureURL = classLoader.getResource(CAPTURE_SOUND_PATH);
            URL checkURL = classLoader.getResource(CHECK_SOUND_PATH);
            URL checkmateURL = classLoader.getResource(CHECKMATE_SOUND_PATH);
            URL newgameURL = classLoader.getResource(NEWGAME_SOUND_PATH);

            AudioInputStream audioInputStreamMove = AudioSystem.getAudioInputStream(moveURL);
            AudioInputStream audioInputStreamCapture = AudioSystem.getAudioInputStream(captureURL);
            AudioInputStream audioInputStreamCheck = AudioSystem.getAudioInputStream(checkURL);
            AudioInputStream audioInputStreamCheckMate = AudioSystem.getAudioInputStream(checkmateURL);
            AudioInputStream audioInputStreamNewGame = AudioSystem.getAudioInputStream(newgameURL);

            moveClip = AudioSystem.getClip();
            captureClip = AudioSystem.getClip();
            checkClip = AudioSystem.getClip();
            checkmateClip = AudioSystem.getClip();
            newgameClip = AudioSystem.getClip();

            moveClip.open(audioInputStreamMove);
            captureClip.open(audioInputStreamCapture);
            checkClip.open(audioInputStreamCheck);
            checkmateClip.open(audioInputStreamCheckMate);
            newgameClip.open(audioInputStreamNewGame);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void playMove(){
        moveClip.stop();
        moveClip.flush();
        moveClip.setFramePosition(0);
        moveClip.start();
    }

    public void playCapture(){
        captureClip.stop();
        captureClip.flush();
        captureClip.setFramePosition(0);
        captureClip.start();
    }

    public void playCheck(){
        checkClip.stop();
        checkClip.flush();
        checkClip.setFramePosition(0);
        checkClip.start();
    }

    public void playCheckMate(){
        checkmateClip.stop();
        checkmateClip.flush();
        checkmateClip.setFramePosition(0);
        checkmateClip.start();
    }

    public void playNewGame(){
        newgameClip.stop();
        newgameClip.flush();
        newgameClip.setFramePosition(0);
        newgameClip.start();
    }
}
