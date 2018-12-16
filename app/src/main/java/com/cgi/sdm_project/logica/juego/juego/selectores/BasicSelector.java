package com.cgi.sdm_project.logica.juego.juego.selectores;

import com.cgi.sdm_project.logica.juego.juego.SelectorRegla;

/**
 * Selector de reglas basico que devuelve una regla al azar sin más.
 *
 * @author Enol García González
 * @version 16-12-2018
 */
public class BasicSelector extends SelectorRegla {

    /*
     * Elige una regla aleatoria
     */
    public String getNombreSiguienteJuego() {
        return juegos[(int) (Math.random() * (juegos.length))];
    }
}