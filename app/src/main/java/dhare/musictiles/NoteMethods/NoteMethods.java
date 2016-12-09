package dhare.musictiles.NoteMethods;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Dylan on 21/10/2014.
 */
public class NoteMethods {
    public int NOTE_HEIGHT;
    public int CAMERA_WIDTH;
    public int CAMERA_HEIGHT;
    public int QUARTER_WIDTH;
    public Scene scene;
    public boolean paused = false;

    long addTime;
    long createdTime = System.currentTimeMillis();
    long moveTime;

    VertexBufferObjectManager VBOM = new VertexBufferObjectManager();

    List<Rectangle> firstLane = new ArrayList<>();
    List<Rectangle> secondLane = new ArrayList<>();
    List<Rectangle> thirdLane = new ArrayList<>();
    List<Rectangle> fourthLane = new ArrayList<>();
    public List<List<Rectangle>> allLanes = Arrays.asList(firstLane, secondLane, thirdLane, fourthLane);


    List<Integer> noteTimesLane1 = new ArrayList<>();
    List<Integer> noteTimesLane2 = new ArrayList<>();
    List<Integer> noteTimesLane3 = new ArrayList<>();
    List<Integer> noteTimesLane4 = new ArrayList<>();
    List<List<Integer>> noteTimesAllLanes = Arrays.asList(noteTimesLane1, noteTimesLane2, noteTimesLane3, noteTimesLane4);
    Random randomGenerater = new Random();

    public void createNoteTimes(int length) {
        for (int i = 0; i < 2000000; i += 480) { //480 //200px to be out of range (note_height=150) 5px/3mili = 1.6, 200/1.6 = 125
            //((max - min) + 1) + min
            if (randomGenerater.nextInt(101) > 30) {
                int sgle = randomGenerater.nextInt(4);
                int dbl = randomGenerater.nextInt(21);
                noteTimesAllLanes.get(sgle).add(i);
                if (dbl < 4 && dbl != sgle)
                    noteTimesAllLanes.get(dbl).add(i);
            }
        }
    }

    public void moveNotes() {
        if (System.currentTimeMillis() - createdTime >= moveTime && !paused) {
            for (int i = 0; i < 4; i++) {
                Iterator<Rectangle> fL = allLanes.get(i).iterator();
                while (fL.hasNext()) {
                    Rectangle rect = fL.next();
                    rect.setY(rect.getY() + 5);
                }
            }
            moveTime = System.currentTimeMillis() - createdTime + 3;
        }
    }

    public void destoryNotes() {
        for (int i = 0; i < 4; i++) {
            if (!allLanes.get(i).isEmpty()) {
                Rectangle rect = allLanes.get(i).get(0);
                if ((rect.getY() > CAMERA_HEIGHT)) {
                    scene.detachChild(rect);
                    allLanes.get(i).remove(rect);
                }
            }
        }
    }

    public void addNotes(int duration) {
       //Long localCreatedTime = System.currentTimeMillis();
        //TODO analyse songs for 4 different pitches/keys/notes
        if (System.currentTimeMillis() - createdTime >= addTime) {
            for (int i = 0; i < 4; i++) {
                if (!noteTimesAllLanes.get(i).isEmpty()) {
                    if (duration >= Long.valueOf(String.valueOf(noteTimesAllLanes.get(i).get(0))))
                    //((max - min) + 1) + min //(3-0) + 1 (== 0--3)
                    {
                        noteTimesAllLanes.get(i).remove(0);
                        Rectangle r = new Rectangle(QUARTER_WIDTH * i, -NOTE_HEIGHT, QUARTER_WIDTH, NOTE_HEIGHT, VBOM);
                        r.setColor(Color.BLACK);
                        r.setZIndex(0);
                        scene.attachChild(r);
                        scene.sortChildren();
                        allLanes.get(i).add(r);
                        /*Rectangle rect = new Rectangle(0, CAMERA_HEIGHT - NOTE_HEIGHT, CAMERA_WIDTH, NOTE_HEIGHT, VBOM);
                        rect.setColor(2, 255, 57, 180);
                        scene.attachChild(rect);
                        scene.detachChild(0);*/
                    }
                }
            }
            addTime = System.currentTimeMillis() - createdTime + 125;
        }
    }
}

