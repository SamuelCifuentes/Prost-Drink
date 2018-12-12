package com.cgi.sdm_project.logica.juego.reglas;

import com.cgi.sdm_project.R;
import com.cgi.sdm_project.util.AppSingleton;

import java.util.ArrayList;
import java.util.List;

public class Votacion extends ReglaTragable {
    private String[] opciones;
    private String texto;
    private int votacion;
    private String votacionQueMostrar;

    public Votacion(List<String> opciones, int tragos, String texto) {
        super(tragos);
        ArrayList<String> aux = new ArrayList<>(opciones);
        this.texto = texto;
        this.opciones = new String[aux.size()];
        for (int i = 0; i < aux.size(); i++)
            this.opciones[i] = aux.get(i);
    }

    public String[] getOpciones() {
        return opciones.clone();
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public String getResultado() {
        try {
            int resource = R.string.class.getField(nombreRespuesta()).getInt(R.string.class);
            return String.format(AppSingleton.getInstance().getContext().getString(resource), votacionQueMostrar, getTragos());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return String.format(AppSingleton.getInstance().getContext().getString(R.string.resultado_generar), getTragos());
    }

    @Override
    protected String nombreRespuesta() {
        if ((int) (Math.random() * 1000) % 2 == 0) {
            votacionQueMostrar = opciones[opciones.length - 1 - votacion];
            return "resultado_votacion_bebe";
        }
        votacionQueMostrar = opciones[votacion];
        return "resultado_votacion_manda";
    }

    public void votar(int i) {
        votacion = i;
    }
}
