package co.edu.uniquindio.compiladores.sintaxis

import javafx.scene.control.TreeItem

class Decision():Sentencia() {

    var expresionLogica: Expresion? = null
    var listaSentencia: ArrayList<Sentencia>? = null
    var listaSentenciaAltro:ArrayList<Sentencia>? = null

    constructor(expresionLogica: Expresion?,listaSentencia: ArrayList<Sentencia>?):this(){
        this.expresionLogica = expresionLogica
        this.listaSentencia = listaSentencia
    }
    constructor(expresionLogica: Expresion?,listaSentencia: ArrayList<Sentencia>?, listaSentenciaAltro:ArrayList<Sentencia>?):this(){
        this.expresionLogica = expresionLogica
        this.listaSentencia = listaSentencia
        this.listaSentenciaAltro = listaSentenciaAltro
    }

    override fun toString(): String {
        return "Desicion(expresionLogica=$expresionLogica, listaSentencia=$listaSentencia)"
    }

    override fun getArbolVisual(): TreeItem<String> {
        var raiz = TreeItem<String>("Desición")

        var condicion = TreeItem("Condición")
        condicion.children.add(expresionLogica?.getArbolVisual())
        raiz.children.add(condicion)

        var raizSentencias= TreeItem("Sentencias Verdaderas")

        for(i in listaSentencia!!){
            raizSentencias.children.add(i.getArbolVisual())
        }
        raiz.children.add(raizSentencias)

        var raizSentenciasA= TreeItem("Sentencias Falsas")
        if(listaSentenciaAltro != null) {
            for (s in listaSentenciaAltro!!) {
                raizSentenciasA.children.add(s.getArbolVisual())
            }
            raiz.children.add(raizSentenciasA)
        }

        return  raiz
    }

}