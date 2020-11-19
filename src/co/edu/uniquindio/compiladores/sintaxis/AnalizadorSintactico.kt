package co.edu.uniquindio.compiladores.sintaxis

import co.edu.uniquindio.compiladores.lexico.Categoria
import co.edu.uniquindio.compiladores.lexico.Error
import co.edu.uniquindio.compiladores.lexico.Token

class AnalizadorSintactico(var listaTokens:ArrayList<Token>) {

    var posicionActual = 0
    var tokenActual = listaTokens[0]
    var listaErrores = ArrayList<Error>()

    fun obtenerSiguienteToken() {

        posicionActual++
        if (posicionActual < listaTokens.size) {
            tokenActual = listaTokens[posicionActual]
        }
    }

    fun reportarError(mensaje: String) {
        listaErrores.add(Error(mensaje, tokenActual.fila, tokenActual.columna))
    }

    /**
     * <UnidadDeCompilacion> ::=  <ListaFunciones>
     */
    fun esUnidadDeCompilacion(): UnidadDeCompilacion? {
        val listaFunciones: ArrayList<Funcion> = esListaFunciones()
        return if (listaFunciones.size > 0) {
            UnidadDeCompilacion(listaFunciones)
        } else null
    }

    /**
     * <ListaFunciones> ::= <Funcion> [<ListaFunciones>]
     */
    fun esListaFunciones(): ArrayList<Funcion> {
        var listaFunciones = ArrayList<Funcion>()
        var funcion = esFuncion()

        while (funcion != null) {
            listaFunciones.add(funcion)
            funcion = esFuncion()
        }
        return listaFunciones
    }

    /**
     * <Desicion> ::= se <Expresiones> <BloqueSentencias> [also <BlouqeSentencias>  ]
     */
    fun esDesicion(): Decision? {
        if (tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "se") {
            obtenerSiguienteToken()
            val expresion = esExpresion()
            if (expresion != null) {
                obtenerSiguienteToken()
                val listaSentencias = esBloqueSentencias()
                if (listaSentencias != null) {
                    if (tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "altro") {
                        obtenerSiguienteToken()
                        val listaSentenciasAltro = esBloqueSentencias()
                        if (listaSentenciasAltro != null) {
                            return Decision(expresion, listaSentencias, listaSentenciasAltro)
                        } else {
                                    reportarError("Falta bloque de sentencias")
                        }

                    } else {
                                return Decision(expresion, listaSentencias, null)
                    }
                } else {
                            reportarError("Falta bloque de sentencias")
                }
            }else{
                reportarError("Falta una Expresion logica")
            }
        }

        return null
    }

    /**
     * <ListaParametros> ::= <Parametro>[","<ListaParametros>]
     */
    fun esFuncion():Funcion?{

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA && tokenActual.lexema == "fun"){
            obtenerSiguienteToken()

            var tipoRetorno = estipoRetorno()

            if(tipoRetorno != null){
                obtenerSiguienteToken()

                if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                    var nombreFuncion = tokenActual
                    obtenerSiguienteToken()

                    if(tokenActual.categoria == Categoria.PARENTESIS_IZQ){
                        obtenerSiguienteToken()

                        var listaParametros = esListaParametros()

                        if(tokenActual.categoria == Categoria.PARENTESIS_DER){
                            obtenerSiguienteToken()

                            var bloqueSentencias= esBloqueSentencias()

                            if (bloqueSentencias != null){
                                return Funcion(nombreFuncion, tipoRetorno, listaParametros, bloqueSentencias)
                            }
                            else{
                                reportarError( "Faltó el bloque de sentencias en la función" )
                            }
                        }
                        else{
                            reportarError("Falta parentesis derecho")
                        }
                    }
                    else{
                        reportarError("Falta parentesis izquierdo")
                    }
                }
                else{
                    reportarError("Falta nombre del metodo")
                }
            }
            else{
                reportarError("Falta el tipo de retorno en el método")
            }
        }
        return null
    }

    /**
     * <TipoRetorno> ::= numZ | numR | chordi | khar | bool |void
     */
    fun estipoRetorno():Token?{

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA){

            if(tokenActual.lexema == "numZ" || tokenActual.lexema == "numR" || tokenActual.lexema == "chordi" || tokenActual.lexema == "khar" || tokenActual.lexema == "bool" || tokenActual.lexema == "void"){
                return tokenActual
            }
        }
        return null
    }

    /**
     * <TipoRetorno> ::= numZ | numR | chordi | khar | bool
     */
    fun estipoDato():Token?{

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA){

            if(tokenActual.lexema == "numZ" || tokenActual.lexema == "numR" || tokenActual.lexema == "chordi" || tokenActual.lexema == "khar" || tokenActual.lexema == "bool" ){
                return tokenActual
            }
        }
        return null
    }

    /**
     * <ListaParametros> ::= <Parametro>[","<ListaParametros>]
     */
    fun esListaParametros():ArrayList<Parametro>?{
        var listaParametros = ArrayList<Parametro>()
        var parametro = esParametro()
        while (parametro != null){
            listaParametros.add(parametro)
            obtenerSiguienteToken()
            if(tokenActual.categoria == Categoria.COMA){
                obtenerSiguienteToken()
                parametro = esParametro()
            }else{
                if(tokenActual.categoria != Categoria.PARENTESIS_DER){
                    reportarError("Falta un parentisis derecho en la lista de parametros")
                }
                break
            }

        }
        return listaParametros
    }

    /**
     * <Parametro> ::= <TipoDato> Identificador
     */
    fun esParametro():Parametro?{

        if(tokenActual.categoria == Categoria.PALABRA_RESERVADA){
            if(tokenActual.lexema == "numZ" || tokenActual.lexema == "numR" || tokenActual.lexema == "chordi" || tokenActual.lexema == "khar" || tokenActual.lexema == "bool"){
                val tipoDato = estipoDato()
                if(tipoDato!=null){
                    obtenerSiguienteToken()
                    if(tokenActual.categoria == Categoria.IDENTIFICADOR){
                        val nombre = tokenActual
                        return Parametro(nombre,tipoDato)
                    }
                }else{
                    reportarError("Falta el tipo de error en el parametro")
                }

            }
        }
        return null
    }

    /**
     * <BloqueSentencias> ::= "<" [<ListaSentencias>] ">"
     */
    fun esBloqueSentencias():ArrayList<Sentencia>?{
        if (tokenActual.categoria == Categoria.LLAVE_IZQ) {
            obtenerSiguienteToken()

            var listaSentencias = esListaSentencias()

            if (tokenActual.categoria == Categoria.LLAVE_DER) {
                obtenerSiguienteToken()

                return listaSentencias
            }
            else{
                reportarError( "Falta llave derecha" )
            }
        }
        else{
            reportarError( "Falta llave izquierda" )
        }
        return null
    }

    /**
     * <Sentencia> ::= <Desicion> | <DeclaracionMetodos> | <DesclaracionVariables> | <Asignación> |
    <Impresión> | <Lectura> | <Retorno> | <Invocaciones> | <CicloMientras> | <CicloFor> |
    <Incremento> | <Decremento>
     */
    fun esSentencia():Sentencia?{

       var s:Sentencia? = esDesicion()
        if(s != null) return s
        /** s = esDeclaracionVariable()
        if(s != null) return s
        s = esAsignacion()
        if(s != null) return s
        s = esImpresion()
        if(s != null) return s
        s = esLectura()
        if(s != null) return s
        s = esRetorno()
        if(s != null) return s
        s = esInvocacion()
        if(s != null) return s
        s = esCicloMientras()
        if(s != null) return s
        s = esIncremento()
        if(s != null) return s
        s = esDecremento()
        return s  */
        return null
    }

    /**
     * <ListaSentencias> ::= <Sentencia>[<ListaSentencias>]
     */
    fun esListaSentencias():ArrayList<Sentencia>?{

        var listaSentencia = ArrayList<Sentencia>()
        var sentencia = esSentencia()

        while (sentencia!=null){
            listaSentencia.add(sentencia)
            sentencia = esSentencia()
        }
       return listaSentencia


    return null
    }

    /**
     * poner bnf
     */
    fun esExpresion():Expresion?{
        var e:Expresion? = esExpresionAritmetica()
        if(e != null) return e
        e = esExpresionLogica()
        if(e != null) return e
        e = esExpresionRelacional()
        if(e != null) return e
        e = esExpresionCadena()
        return e
    }

    /**
     * Poner bnf
     */
    fun esExpresionAritmetica():ExpresionAritmetica?{
        if (tokenActual.categoria == Categoria.PARENTESIS_IZQ){
            obtenerSiguienteToken()
            val expresion1 = esExpresionAritmetica()
            if(expresion1 != null){

                if(tokenActual.categoria == Categoria.PARENTESIS_DER){
                    obtenerSiguienteToken()
                    if(tokenActual.categoria == Categoria.OPERADOR_ARITMETICO){
                        val operador = tokenActual
                        obtenerSiguienteToken()
                        val expresion2 = esExpresionAritmetica()
                        if(expresion2 != null){
                            return ExpresionAritmetica(expresion1,operador,expresion2)
                        }
                    }else{
                        return ExpresionAritmetica(expresion1)
                    }
                }
            }
        }else{
            val valor = esValorNumerico()
            if(valor != null){
                obtenerSiguienteToken()
                if(tokenActual.categoria == Categoria.OPERADOR_ARITMETICO){
                    val operador = tokenActual
                    obtenerSiguienteToken()
                    val expresion = esExpresionAritmetica()
                    if(expresion != null){
                        return ExpresionAritmetica(valor,operador,expresion)
                    }
                }else{
                    return ExpresionAritmetica(valor)
                }
            }
        }
        return null
    }

    /**
     * <ExpresionLogica> ::= <ExpresionLogica>[operadorLogico <ExpresionLogica>]|
     * <valoirLogico> [operadorLogico <ExpresionLogica>]| <ExpresionRelacionla>[operadorLogico <ExpresionLogica>]
     */
    fun esExpresionLogica():ExpresionLogica?{
        if(tokenActual.categoria == Categoria.PARENTESIS_IZQ) {
            obtenerSiguienteToken()
            val expL = esExpresionLogica()
            if (expL != null) {
                obtenerSiguienteToken()
                if (tokenActual.categoria == Categoria.OPERADOR_LOGICO) {
                    val operadorL = tokenActual
                    obtenerSiguienteToken()
                    val expL2 = esExpresionLogica()
                    if (expL2 != null) {
                        return ExpresionLogica(expL, operadorL, expL2)
                    }
                } else {
                    return ExpresionLogica(expL)
                }
            } else {
                val expR = esExpresionRelacional()
                if (expR != null) {
                    obtenerSiguienteToken()
                    if (tokenActual.categoria == Categoria.OPERADOR_LOGICO) {
                        val operadorL = tokenActual
                        obtenerSiguienteToken()
                        val expL2 = esExpresionLogica()
                        if (expL2 != null) {
                            return ExpresionLogica(expR, operadorL, expL2)
                        }
                    } else {
                        return ExpresionLogica(expR)
                    }
                }

            }
            if (tokenActual.categoria == Categoria.PALABRA_RESERVADA && (tokenActual.lexema == "True" || tokenActual.lexema == "False")){
                val valorLogico = tokenActual
                obtenerSiguienteToken()
                if (tokenActual.categoria == Categoria.OPERADOR_LOGICO) {
                    val operadorL = tokenActual
                    obtenerSiguienteToken()
                    val expL2 = esExpresionLogica()
                    if (expL2 != null) {
                        return ExpresionLogica(valorLogico, operadorL, expL2)
                    }
                } else {
                    return ExpresionLogica(valorLogico)
                }
            }
        }
        return null
    }

    /**
     * <ExpresiónRelacional> ::= <ExpresionAritmetica> operadorRelacional <ExpresionAritmetica> | <ExpresionCadena> operadorRelacional <Expresioncadena>
     */
    fun esExpresionRelacional():ExpresionRelacional?{
        var exp1= esExpresionAritmetica()
        if(exp1 != null){
            obtenerSiguienteToken()
            print(tokenActual.lexema)
            if(tokenActual.categoria == Categoria.OPERADORES_RELACIONALES){
                val operadorRelacional = tokenActual
                obtenerSiguienteToken()
                val exp2 = esExpresionAritmetica()
                if(exp2 != null){
                    return ExpresionRelacional(exp1,operadorRelacional,exp2)
                }
            }
        }
        val expC1 = esExpresionCadena()
        if(expC1 != null){
            obtenerSiguienteToken()
            if(tokenActual.categoria == Categoria.OPERADORES_RELACIONALES && (tokenActual.lexema == "¬;" || tokenActual.lexema == ";;")){
                val operadorRelacional = tokenActual
                obtenerSiguienteToken()
                val expC2 = esExpresionCadena()
                if(expC2 != null){
                    return ExpresionRelacional(expC1,operadorRelacional,expC2)
                }
            }
        }

        return null
    }

    /**
     *  <ExpresionCadena> ::= cadenaDeCaracteres ["+"<Expresion> ] | <Expresion> “+”cadenaDeCaracteres
     */
    fun esExpresionCadena():ExpresionCadena?{
        return null
    }

    fun esValorNumerico():ValorNumerico?{
        var signo:Token?=null
        if(tokenActual.categoria == Categoria.OPERADOR_ARITMETICO && (tokenActual.lexema == "+" || tokenActual.lexema == "-")){
            signo = tokenActual
            obtenerSiguienteToken()
        }
        if(tokenActual.categoria == Categoria.ENTERO || tokenActual.categoria == Categoria.DECIMAL || tokenActual.categoria == Categoria.IDENTIFICADOR ){
            val valor = tokenActual
            return ValorNumerico(signo, valor)
        }
        return null
    }
}