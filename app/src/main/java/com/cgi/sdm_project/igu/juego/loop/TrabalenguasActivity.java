package com.cgi.sdm_project.igu.juego.loop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cgi.sdm_project.R;
import com.cgi.sdm_project.logica.juego.Juego;
import com.cgi.sdm_project.logica.juego.activities.ContinuarRonda;
import com.cgi.sdm_project.logica.juego.activities.InicioJuego;
import com.cgi.sdm_project.logica.juego.reglas.implementaciones.Trabalenguas;
import com.cgi.sdm_project.util.PermissionChecker;
import com.cgi.sdm_project.util.factories.FactoryStemmer;
import com.cgi.sdm_project.util.stemmers.Stemmer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

public class TrabalenguasActivity extends LoopSinMusica implements InicioJuego, TextToSpeech.OnInitListener {
    private Trabalenguas trabalenguas;
    private FloatingActionButton speakButton;
    private Button continuar;
    private TextView inputText, trabalenguasTxt, intentosTxt;

    private SpeechRecognizer sr;
    private TextToSpeech tts;
    private Stemmer stemmer;
    private List<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trabalenguas);
        trabalenguas = (Trabalenguas) Juego.getInstance().getJuegoActual();

        ((TextView) findViewById(R.id.txtJugador)).setText(Juego.getInstance().getJugadorActual().toString());
        ((TextView) findViewById(R.id.lblTrabalenguas)).setText(trabalenguas.getTexto());

        //Asignación atributos
        intentosTxt = findViewById(R.id.txtIntNum);
        inputText = findViewById(R.id.txtInput);
        trabalenguasTxt = findViewById(R.id.lblTrabalenguas);
        speakButton = findViewById(R.id.btnSpeak);
        continuar = findViewById(R.id.btnContinuar);

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(v);
            }
        });


        //Muestra número de intentos inicial
        intentosTxt.setText(String.valueOf(trabalenguas.getIntentos()));

        //Reconocedor de voz
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new SRListener());

        //TTS
        tts = new TextToSpeech(this, this);

        //Selecciono Stemmer en función del idioma del juego
        stemmer = FactoryStemmer.getStemmer();

    }

    /**
     * Libero los recursos asignados al reconocedor de voz y el tts
     */
    @Override
    protected void onDestroy() {
        trabalenguas.resetIntentos();
        sr.destroy();
        tts.shutdown();
        super.onDestroy();
    }

    /**
     * Reproduce el trabalenguas mediante TTS
     *
     * @param v
     */
    public void ttsEvent(View v) {
        tts.speak(trabalenguasTxt.getText().toString(), TextToSpeech.QUEUE_ADD,
                null, null);
    }

    /**
     * Evento del fab de escuchar, comprueba si tenemos permisos de grabación de audio, si no, los
     * pide. Llama al método que gestiona la entrada de audio
     *
     * @param v
     */
    public void speak(View v) {
        PermissionChecker permissionChecker = PermissionChecker.getInstance();
        if (!permissionChecker.isRecordPermissionGranted(this)) {
            permissionChecker.pedirPermisos(PermissionChecker.RECORD_LOC, PermissionChecker.MY_PERMISSIONS_RECORD, this);
        } else {
            listenToSweetVoice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        PermissionChecker.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
        listenToSweetVoice();
    }

    /**
     * Escucha tu dulce voz
     */
    private void listenToSweetVoice() {
        if (!PermissionChecker.getInstance().isRecordPermissionGranted(this)) {
            new ContinuarRonda().cargarSiguienteJuego(null);
            finish();
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        sr.startListening(intent);
    }

    /**
     * Procesa entrada de audio por micrófono
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ArrayList<String> matches =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            checkRespuesta(matches);
        }
    }

    /**
     * Comprueba entrada y la compara con el trabalenguas de la regla, si el resultado es correcto
     * se pinta en verde y se permite el progreso al siguiente juego.
     * Si no es correcto, se recalcula el número de intentos restantes y si se llega a 0 intentos,
     * se desbloquea el botón de continuar.
     *
     * @param matches
     * @return
     */
    private boolean checkRespuesta(List<String> matches) {
        Collator c = Collator.getInstance();
        c.setStrength(Collator.PRIMARY);
        for (String m : matches) {
            if (c.equals(stemmer.stem(m.replaceAll("[¡!¿?,.:;]", "")),
                    stemmer.stem(trabalenguas.getTexto().replaceAll("[¡!¿?,.:;]", "")))) {
                inputText.setText(m);
                inputText.setTextColor(Color.GREEN);
                continuar.setVisibility(View.VISIBLE);
                speakButton.setClickable(false);
                trabalenguas.atreverse();
                return true;
            }
        }
        inputText.setText(matches.get(0));
        inputText.setTextColor(Color.RED);
        boolean flag = trabalenguas.reducirIntentos();
        intentosTxt.setText(String.valueOf(trabalenguas.getIntentos()));
        if (flag) {
            continuar.setVisibility(View.VISIBLE);
            intentosTxt.setTextColor(Color.RED);
            speakButton.setClickable(false);
        }
        return false;
    }

    public void continuar(View view) {
        Intent mIntent = new Intent(getApplicationContext(), ResultadoActivity.class);
        startActivity(mIntent);
        finish();
    }

    @Override
    public void onInit(int status) {

    }

    //Configura el reconocedor de voz
    class SRListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onResults(Bundle results) {
            matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            checkRespuesta(matches);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }
}
