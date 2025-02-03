/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.infotarghe.ui.rileva;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.infotarghe.R;
import com.example.infotarghe.SharedViewModel;
import com.example.infotarghe.customview.OverlayView;
import com.example.infotarghe.env.BorderedText;
import com.example.infotarghe.env.ImageUtils;
import com.example.infotarghe.env.Logger;
import com.example.infotarghe.tracking.MultiBoxTracker;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorFragment extends CameraFragment implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "plate_recognizer.tflite";
  private static final String TF_OD_API_LABELS_FILE = "label_map.pbtxt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.9f;
  private static final boolean MAINTAIN_ASPECT = true;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(480, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Detector detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  private TextView ocrPreviewWindow;


  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(getContext());

    int cropSize = TF_OD_API_INPUT_SIZE;

    // Initialize the detector model
    try {
      detector =
              TFLiteObjectDetectionAPIModel.create(
                      getContext(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_INPUT_SIZE,
                      TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing Detector!");
      Toast toast =
              Toast.makeText(
                      getContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      getActivity().finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropSize, cropSize,
                    sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = getView().findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new OverlayView.DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  private void recognizeTextFromBitmap(Bitmap bitmap) {
    InputImage image = InputImage.fromBitmap(bitmap, 0);
    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    recognizer.process(image)
            .addOnSuccessListener(result -> {
              String recognizedText = result.getText();

              // Controlla che il Fragment sia ancora attivo prima di accedere al ViewModel
              if (!isAdded()) return;

              SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

              if (!recognizedText.isEmpty()) {
                String filteredText = filterPlateText(recognizedText); // Filtra il testo

                if (isPlateAllowed(recognizedText)) {
                  updateOCRWindow(filteredText); // Mostra il testo filtrato nell'anteprima OCR
                  long timestamp = System.currentTimeMillis();
                  viewModel.addValidPlate(filteredText, timestamp); // Usa il testo filtrato
                } else {
                  updateOCRWindow("ERROR");

                  // Incrementa il contatore di targhe non valide
                  viewModel.incrementInvalidPlates();
                }
              } else {
                updateOCRWindow("NO TEXT");
                viewModel.incrementInvalidPlates();
              }
            })
            .addOnFailureListener(e -> {
              LOGGER.e("Errore OCR: " + e.getMessage());
              updateOCRWindow("Errore nel riconoscimento del testo.");

              // Controlla che il Fragment sia ancora attivo prima di accedere al ViewModel
              if (!isAdded()) return;
              
              new ViewModelProvider(requireActivity()).get(SharedViewModel.class).incrementInvalidPlates();
            });
  }




  private void updateOCRWindow(String text) {
    getActivity().runOnUiThread(() -> {
      TextView ocrPreviewWindow = getView().findViewById(R.id.ocr_preview_window);
      if (ocrPreviewWindow != null) {
        ocrPreviewWindow.setText(text); // Aggiorna il testo nel TextView
      } else {
        LOGGER.e("OCR Preview Window non trovato.");
      }
    });
  }

  /**
   * Il seguente metodo filtra il testo fornito per eliminare qualsiasi carattere che non sia alfanumerico
   * e converte tutti i caratteri in maiuscolo.
   *Motivo:
   * - Le targhe automobilistiche valide seguono un formato alfanumerico standard, composto da lettere
   *   e numeri senza spazi o simboli speciali.
   * - Questo metodo standardizza il testo per garantire che il confronto con il pattern sia accurato.
   *
   * @param text Il testo grezzo da filtrare.
   * @return Una stringa contenente solo caratteri alfanumerici in maiuscolo.
   */
  private String filterPlateText(String text) {
    if (text == null) {
      return ""; // Restituisci stringa vuota se il testo è null
    }
    // Rimuovi tutto ciò che non è una lettera o un numero, e trasforma in maiuscolo
    return text.replaceAll("[^A-Z0-9]", "").toUpperCase();
  }


  private boolean isPlateAllowed(String text) {
    // Filtra il testo per ottenere una versione pulita
    String filteredText = filterPlateText(text);
    LOGGER.i("Testo originale dall'OCR: " + text);
    LOGGER.i("Testo filtrato per isPlateAllowed: " + filteredText);

    // Espressione regolare per il formato di targa europea
    String platePattern = "^[A-Z]{2}[0-9]{3}[A-Z]{2}$";
    Pattern pattern = Pattern.compile(platePattern);

    // Cerca un match con il pattern
    Matcher matcher = pattern.matcher(filteredText);
    if (matcher.matches()) { // Usa .matches() per verificare tutta la stringa
      LOGGER.i("Targa valida riconosciuta: " + filteredText);
      return true; // È una targa valida
    }

    LOGGER.e("Nessuna targa valida trovata nel testo filtrato: " + filteredText);
    return false;
  }



  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

    runInBackground(() -> {
      final long startTime = SystemClock.uptimeMillis();
      final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
      lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

      cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
      final Canvas canvas1 = new Canvas(cropCopyBitmap);
      final Paint paint = new Paint();
      paint.setColor(Color.RED);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(2.0f);

      float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
      final List<Detector.Recognition> mappedRecognitions = new ArrayList<>();

      for (final Detector.Recognition result : results) {
        final RectF location = result.getLocation();

        if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
          // Log delle coordinate originali (crop space)
          LOGGER.i("Bounding box originale (crop space): " + location.toString());

          // Trasformiamo le coordinate nel frame space (usate per il tracker e l'overlay)
          cropToFrameTransform.mapRect(location);

          // Log delle coordinate trasformate
          LOGGER.i("Bounding box trasformato (frame space): " + location.toString());

          // Crea una copia delle coordinate per il ritaglio
          RectF locationForCrop = new RectF(location);
          frameToCropTransform.mapRect(locationForCrop);

          // Log delle coordinate per il ritaglio
          LOGGER.i("Bounding box per il ritaglio (crop space): " + locationForCrop.toString());

          // Verifica larghezza e altezza del bounding box per il ritaglio
          if (locationForCrop.width() > 0 && locationForCrop.height() > 0) {
            try {
              Bitmap boundingBoxBitmap = Bitmap.createBitmap(
                      croppedBitmap,
                      (int) Math.max(locationForCrop.left, 0),
                      (int) Math.max(locationForCrop.top, 0),
                      (int) Math.min(locationForCrop.width(), croppedBitmap.getWidth() - locationForCrop.left),
                      (int) Math.min(locationForCrop.height(), croppedBitmap.getHeight() - locationForCrop.top)
              );

              // Deforma il ritaglio per adattarlo al TextView OCR
              Bitmap scaledBitmap = Bitmap.createScaledBitmap(boundingBoxBitmap, 200, 50, false);

              // Passa il ritaglio all'OCR per il riconoscimento del testo
              recognizeTextFromBitmap(scaledBitmap);

            } catch (IllegalArgumentException e) {
              LOGGER.e("Errore nel ritaglio del bounding box: " + e.getMessage());
            }
          } else {
            LOGGER.e("Bounding box non valido per il ritaglio: " + locationForCrop.toString());
          }

          // Aggiungi il bounding box trasformato alla lista per il tracker
          mappedRecognitions.add(new Detector.Recognition(
                  result.getId(),
                  result.getTitle(),
                  result.getConfidence(),
                  location
          ));
        }
      }


// Passa i risultati trasformati al tracker
      tracker.trackResults(mappedRecognitions, currTimestamp);
      trackingOverlay.postInvalidate();



      tracker.trackResults(mappedRecognitions, currTimestamp);
      trackingOverlay.postInvalidate();
      computingDetection = false;

      getActivity().runOnUiThread(() -> {
        showFrameInfo(previewWidth + "x" + previewHeight);
        showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
        showInference(lastProcessingTimeMs + "ms");
      });
    });
  }


  @Override
  public synchronized void onResume() {
    super.onResume();
    computingDetection = false;

    // Inizializza l'ImageView quando il fragment è attivo
    if (getView() != null) {
      ocrPreviewWindow = getView().findViewById(R.id.ocr_preview_window);

      if (ocrPreviewWindow == null) {
        LOGGER.e("ocrPreviewWindow is null! Verifica che l'ID sia corretto nel layout XML.");
      } else {
        LOGGER.i("ocrPreviewWindow inizializzato correttamente.");
      }
    }
  }


  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(
            () -> {
              try {
                detector.setUseNNAPI(isChecked);
              } catch (UnsupportedOperationException e) {
                LOGGER.e(e, "Failed to set \"Use NNAPI\".");
                getActivity().runOnUiThread(
                        () -> {
                          Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        });
              }
            });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
}
