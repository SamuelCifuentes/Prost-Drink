package com.cgi.sdm_project.igu.juego.loop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cgi.sdm_project.R;
import com.cgi.sdm_project.logica.juego.Juego;
import com.cgi.sdm_project.logica.juego.activities.InicioJuego;
import com.cgi.sdm_project.logica.juego.reglas.implementaciones.Votacion;

public class VotacionActivity extends Loop implements InicioJuego {
    private Votacion votacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_votacion);
        votacion = (Votacion) Juego.getInstance().getJuegoActual();

        ((TextView) findViewById(R.id.txtJugador)).setText(Juego.getInstance().getJugadorActual().toString());
        ((TextView) findViewById(R.id.txtTextoVotacion)).setText(votacion.getTexto());
        ((Button) findViewById(R.id.btnVotacionOpA)).setText(votacion.getOpciones()[0]);
        ((Button) findViewById(R.id.btnVotacionOpB)).setText(votacion.getOpciones()[1]);
    }

    private void siguiente(View v) {
        Intent mIntent = new Intent(getApplicationContext(), ResultadoActivity.class);
        startActivity(mIntent);
        finish();
    }

    public void opcionA(View v) {
        votacion.votar(0);
        siguiente(v);
    }

    public void opcionB(View v) {
        votacion.votar(1);
        siguiente(v);
    }
}
