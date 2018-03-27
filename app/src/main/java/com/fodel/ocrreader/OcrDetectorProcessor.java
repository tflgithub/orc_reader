/*
 * Copyright (C) The Android Open Source Project
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
package com.fodel.ocrreader;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private Set<String> processResultSet = new HashSet<>();

    private OcrDetectorListener mOcrDetectorListener;

    OcrDetectorProcessor(OcrDetectorListener ocrDetectorListener) {
        mOcrDetectorListener = ocrDetectorListener;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        processResultSet.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                processResultSet.add(item.getValue());
            }
        }
        UnitedArabEmiratesIDCard unitedArabEmiratesIDCard = recognitionCompleted(new ArrayList<>(processResultSet));
        mOcrDetectorListener.onDetectorSuccess(unitedArabEmiratesIDCard);
    }

    private UnitedArabEmiratesIDCard recognitionCompleted(List<String> list) {
        UnitedArabEmiratesIDCard unitedArabEmiratesIDCard=null;
        for (String str : list) {
            String IDCardNumber = detectedIDCardNumber(str);
            String name = detectedName(str);
            if (IDCardNumber != null) {
                unitedArabEmiratesIDCard = new UnitedArabEmiratesIDCard();
                unitedArabEmiratesIDCard.IDCardNumber=IDCardNumber;
                return unitedArabEmiratesIDCard;
            }
            if (name != null) {
                unitedArabEmiratesIDCard = new UnitedArabEmiratesIDCard();
                unitedArabEmiratesIDCard.name=name;
            }
        }
        return unitedArabEmiratesIDCard;
    }

    private String detectedIDCardNumber(String value) {
        String idCardNumber = null;
        if (stringFilter(value).contains("-")) {
            String[] idCardNumberArray = value.split("-");
            if (idCardNumberArray.length == 4) {
                if (idCardNumberArray[0].length() == 3 && idCardNumberArray[1].length() == 4 && idCardNumberArray[2].length() == 7 && idCardNumberArray[3].length() == 1) {
                    idCardNumber = value.replace("-", "");
                }
            }
        }
        return idCardNumber;
    }

    private String detectedName(String value) {
        String name = null;
        if (value.contains("Name:")) {
            String[] nameArray = value.split(":");
            name = nameArray[1];
        }
        return name;
    }

    @NonNull
    private String stringFilter(String str) {
        Pattern p = Pattern.compile("\n");
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        processResultSet.clear();
    }
}
