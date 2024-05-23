package com.polar.polarsdkecghrdemo.helpers;

import com.google.mlkit.vision.face.Face;

public class DetectionResult {
    private String barcodeMessage = " ";
    private Face mainFace = null;

    public Face getMainFace() {
        return mainFace;
    }

    public String getBarcodeMessage() {
        return barcodeMessage;
    }

    public void setBarcodeMessage(String barcodeMessage) {
        this.barcodeMessage = barcodeMessage;
    }

    public void setMainFace(Face mainFace) {
        this.mainFace = mainFace;
    }
}
