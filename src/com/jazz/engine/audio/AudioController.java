package com.jazz.engine.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for playing audio files.
 */
public class AudioController {
    private Clip moveClip;
    private Clip captureClip;
    private Clip checkClip;
    private Clip checkmateClip;
    private Clip newgameClip;

    private static final String MOVE_SOUND_PATH = "assets/sounds/2.0/move.wav";
    private static final String CAPTURE_SOUND_PATH = "assets/sounds/2.0/capture.wav";
    private static final String CHECK_SOUND_PATH = "assets/sounds/2.0/check.wav";
    private static final String CHECKMATE_SOUND_PATH = "assets/sounds/2.0/checkmate.wav";
    private static final String NEWGAME_SOUND_PATH = "assets/sounds/2.0/newgame.wav";

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
            AudioInputStream audioInputStreamMove = AudioSystem.getAudioInputStream(new File(MOVE_SOUND_PATH).getAbsoluteFile());
            AudioInputStream audioInputStreamCapture = AudioSystem.getAudioInputStream(new File(CAPTURE_SOUND_PATH).getAbsoluteFile());
            AudioInputStream audioInputStreamCheck = AudioSystem.getAudioInputStream(new File(CHECK_SOUND_PATH).getAbsoluteFile());
            AudioInputStream audioInputStreamCheckMate = AudioSystem.getAudioInputStream(new File(CHECKMATE_SOUND_PATH).getAbsoluteFile());
            AudioInputStream audioInputStreamNewGame = AudioSystem.getAudioInputStream(new File(NEWGAME_SOUND_PATH).getAbsoluteFile());

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
