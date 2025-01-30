package com.example.infotarghe.customview;

import org.tensorflow.lite.examples.detection.tflite.Detector;

import java.util.List;

public interface ResultsView {
  public void setResults(final List<Detector.Recognition> results);
}
