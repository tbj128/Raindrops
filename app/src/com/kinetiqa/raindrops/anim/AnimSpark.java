/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kinetiqa.raindrops.anim;

import java.util.HashMap;

import android.graphics.Bitmap;

/**
 * This class represents a single Droidflake, with properties representing its
 * size, rotation, location, and speed.
 */
public class AnimSpark {

    // These are the unique properties of any flake: its size, rotation, speed,
    // location, and its underlying Bitmap object
    float x, y;
    float rotation;
    float speed;
    float yspeed;
    float xspeed;
    float alpha;
    float rotationSpeed;
    int width, height;
    Bitmap bitmap;

    // This map stores pre-scaled bitmaps according to the width. No reason to create
    // new bitmaps for sizes we've already seen.
    static HashMap<Integer, Bitmap> bitmapMap = new HashMap<Integer, Bitmap>();

    /**
     * Creates a new droidflake in the given xRange and with the given bitmap. Parameters of
     * location, size, rotation, and speed are randomly determined.
     */
    static AnimSpark createFlake(float xRange, float yRange, Bitmap originalBitmap) {
        AnimSpark flake = new AnimSpark();
        // Size each flake with a width between 5 and 55 and a proportional height
        flake.width = (int)(50 + (float)Math.random() * 50);
        float hwRatio = originalBitmap.getHeight() / originalBitmap.getWidth();
        flake.height = (int)(flake.width * hwRatio);

        flake.alpha = 255;
        
        // Position the flake horizontally between the left and right of the range
        flake.x = (float)Math.random() * (xRange - flake.width);
        // Position the flake vertically slightly off the bottom of the display
        flake.y = yRange + (flake.height + (float)Math.random() * flake.height);

        // Sparks will start rotation between -90 and 90
        flake.rotation = (float) Math.random() * 180 - 90;
        // flake.rotation = 0;
        flake.rotationSpeed = 0;
        
        // Each flake travels at 50-200 pixels per second
        flake.speed = 200 + (float) Math.random() * 600;
        flake.yspeed = flake.speed * (float) Math.cos(Math.toRadians(flake.rotation));
        flake.xspeed = flake.speed * (float) Math.sin(Math.toRadians(flake.rotation));
        
        // Get the cached bitmap for this size if it exists, otherwise create and cache one
        flake.bitmap = bitmapMap.get(flake.width);
        if (flake.bitmap == null) {
            flake.bitmap = Bitmap.createScaledBitmap(originalBitmap,
                    (int) flake.width, (int) flake.height, true);
            bitmapMap.put(flake.width, flake.bitmap);
        }
        return flake;
    }
}
