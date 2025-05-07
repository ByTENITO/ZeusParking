package com.example.parquiatenov10

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


object Responsividad {

    enum class Breakpoint(val minWidth: Int) {
        XS(0),      // Extra small (móviles pequeños)
        SM(360),    // Small (móviles)
        MD(600),    // Medium (tablets pequeñas)
        LG(840),    // Large (tablets)
        XL(1024),   // Extra large (tablets grandes)
        XXL(1280)   // Extra extra large (pantallas muy grandes)
    }

    // Tipos de dispositivos
    enum class TipoDispositivo {
        MOVIL,
        TABLET,
        DESKTOP
    }

    // Estados de orientación
    enum class Orientacion {
        PORTRAIT,
        LANDSCAPE
    }

    // Datos observables
    private val _breakpointActual = MutableLiveData<Breakpoint>()
    val breakpointActual: LiveData<Breakpoint> = _breakpointActual

    private val _orientacion = MutableLiveData<Orientacion>()
    val orientacion: LiveData<Orientacion> = _orientacion

    private val _tipoDispositivo = MutableLiveData<TipoDispositivo>()
    val tipoDispositivo: LiveData<TipoDispositivo> = _tipoDispositivo

    private val _densidadPantalla = MutableLiveData<Float>()
    val densidadPantalla: LiveData<Float> = _densidadPantalla

    private val _anchoPantallaPx = MutableLiveData<Int>()
    val anchoPantallaPx: LiveData<Int> = _anchoPantallaPx

    private val _altoPantallaPx = MutableLiveData<Int>()
    val altoPantallaPx: LiveData<Int> = _altoPantallaPx

    private val _anchoPantallaDp = MutableLiveData<Int>()
    val anchoPantallaDp: LiveData<Int> = _anchoPantallaDp

    private val _altoPantallaDp = MutableLiveData<Int>()
    val altoPantallaDp: LiveData<Int> = _altoPantallaDp

    private var isInitialized = false

    /**
     * Inicializa el sistema de responsividad con el contexto de la aplicación
     */
    fun inicializar(context: Context) {
        if (isInitialized) return

        actualizarMedidas(context)
        isInitialized = true
    }

    /**
     * Actualiza todas las medidas según el contexto actual
     */
    fun actualizarMedidas(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        // Actualizar dimensiones en píxeles
        _anchoPantallaPx.value = displayMetrics.widthPixels
        _altoPantallaPx.value = displayMetrics.heightPixels
        _densidadPantalla.value = displayMetrics.density

        // Calcular dimensiones en dp
        val anchoDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val altoDp = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        _anchoPantallaDp.value = anchoDp
        _altoPantallaDp.value = altoDp

        // Determinar orientación
        _orientacion.value = if (anchoDp > altoDp) Orientacion.LANDSCAPE else Orientacion.PORTRAIT

        // Determinar breakpoint actual
        _breakpointActual.value = when {
            anchoDp >= Breakpoint.XXL.minWidth -> Breakpoint.XXL
            anchoDp >= Breakpoint.XL.minWidth -> Breakpoint.XL
            anchoDp >= Breakpoint.LG.minWidth -> Breakpoint.LG
            anchoDp >= Breakpoint.MD.minWidth -> Breakpoint.MD
            anchoDp >= Breakpoint.SM.minWidth -> Breakpoint.SM
            else -> Breakpoint.XS
        }

        // Determinar tipo de dispositivo
        _tipoDispositivo.value = when {
            esTablet(context) -> TipoDispositivo.TABLET
            esDesktop(context) -> TipoDispositivo.DESKTOP
            else -> TipoDispositivo.MOVIL
        }
    }

    /**
     * Detecta si un dispositivo es tablet basado en tamaño de pantalla y densidad
     */
    private fun esTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        val esTabletSegunConfig = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE

        val displayMetrics = context.resources.displayMetrics
        val anchuraPulgadas = displayMetrics.widthPixels / displayMetrics.xdpi
        val alturaPulgadas = displayMetrics.heightPixels / displayMetrics.ydpi
        val tamañoDiagonalPulgadas = Math.sqrt((anchuraPulgadas * anchuraPulgadas + alturaPulgadas * alturaPulgadas).toDouble())

        // Se considera tablet si la diagonal es 7 pulgadas o más
        return esTabletSegunConfig || tamañoDiagonalPulgadas >= 7.0
    }

    /**
     * Detecta si es posiblemente un desktop (ChromeOS, Android en PC, etc)
     */
    private fun esDesktop(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.keyboard == Configuration.KEYBOARD_QWERTY &&
                configuration.touchscreen == Configuration.TOUCHSCREEN_FINGER
    }

    /**
     * Calcula el tamaño adaptativo basado en el ancho de pantalla y breakpoint
     */
    fun calcularTamañoAdaptativo(baseSize: Float, factorCrecimiento: Float = 0.2f): Float {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        val factor = when (breakpoint) {
            Breakpoint.XS -> 0.8f
            Breakpoint.SM -> 1.0f
            Breakpoint.MD -> 1.0f + factorCrecimiento
            Breakpoint.LG -> 1.0f + (factorCrecimiento * 2)
            Breakpoint.XL -> 1.0f + (factorCrecimiento * 3)
            Breakpoint.XXL -> 1.0f + (factorCrecimiento * 4)
        }
        return baseSize * factor
    }

    /**
     * Calcula padding adaptativo según el breakpoint actual
     */
    fun calcularPaddingAdaptativo(basePadding: Int): Int {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        return when (breakpoint) {
            Breakpoint.XS -> (basePadding * 0.8f).toInt()
            Breakpoint.SM -> basePadding
            Breakpoint.MD -> (basePadding * 1.2f).toInt()
            Breakpoint.LG -> (basePadding * 1.5f).toInt()
            Breakpoint.XL -> (basePadding * 1.8f).toInt()
            Breakpoint.XXL -> (basePadding * 2f).toInt()
        }
    }

    /**
     * Calcula margen adaptativo según el breakpoint actual
     */
    fun calcularMargenAdaptativo(baseMargin: Int): Int {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        return when (breakpoint) {
            Breakpoint.XS -> (baseMargin * 0.7f).toInt()
            Breakpoint.SM -> baseMargin
            Breakpoint.MD -> (baseMargin * 1.3f).toInt()
            Breakpoint.LG -> (baseMargin * 1.6f).toInt()
            Breakpoint.XL -> (baseMargin * 2f).toInt()
            Breakpoint.XXL -> (baseMargin * 2.5f).toInt()
        }
    }

    /**
     * Determina cuántas columnas mostrar en un grid según el tamaño de pantalla
     */
    fun calcularColumnasGrid(baseColumns: Int = 2): Int {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        return when (breakpoint) {
            Breakpoint.XS -> max(1, baseColumns - 1)
            Breakpoint.SM -> baseColumns
            Breakpoint.MD -> baseColumns + 1
            Breakpoint.LG -> baseColumns + 2
            Breakpoint.XL -> baseColumns + 3
            Breakpoint.XXL -> baseColumns + 4
        }
    }

    /**
     * Calcula un tamaño de texto adaptativo
     */
    fun calcularTamañoTextoAdaptativo(baseSizeSp: Float): Float {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        return when (breakpoint) {
            Breakpoint.XS -> baseSizeSp * 0.9f
            Breakpoint.SM -> baseSizeSp
            Breakpoint.MD -> baseSizeSp * 1.1f
            Breakpoint.LG -> baseSizeSp * 1.2f
            Breakpoint.XL -> baseSizeSp * 1.3f
            Breakpoint.XXL -> baseSizeSp * 1.4f
        }
    }

    /**
     * Determina si debe mostrarse una versión compacta de un componente
     */
    fun deberiaUsarLayoutCompacto(): Boolean {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        return breakpoint == Breakpoint.XS || breakpoint == Breakpoint.SM
    }

    /**
     * Devuelve un porcentaje del ancho de pantalla
     */
    fun porcentajeAnchoPantalla(porcentaje: Float): Int {
        val ancho = anchoPantallaDp.value ?: 360
        return (ancho * porcentaje / 100f).toInt()
    }

    /**
     * Devuelve un porcentaje del alto de pantalla
     */
    fun porcentajeAltoPantalla(porcentaje: Float): Int {
        val alto = altoPantallaDp.value ?: 640
        return (alto * porcentaje / 100f).toInt()
    }

    /**
     * Convierte dp a píxeles
     */
    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    /**
     * Convierte píxeles a dp
     */
    fun pxToDp(context: Context, px: Int): Float {
        return px / context.resources.displayMetrics.density
    }


    fun <T> T.ajustarSegunBreakpoint(
        accionXS: ((T) -> Unit)? = null,
        accionSM: ((T) -> Unit)? = null,
        accionMD: ((T) -> Unit)? = null,
        accionLG: ((T) -> Unit)? = null,
        accionXL: ((T) -> Unit)? = null,
        accionXXL: ((T) -> Unit)? = null
    ): T {
        val breakpoint = breakpointActual.value ?: Breakpoint.SM
        when (breakpoint) {
            Breakpoint.XS -> accionXS?.invoke(this)
            Breakpoint.SM -> accionSM?.invoke(this)
            Breakpoint.MD -> accionMD?.invoke(this)
            Breakpoint.LG -> accionLG?.invoke(this)
            Breakpoint.XL -> accionXL?.invoke(this)
            Breakpoint.XXL -> accionXXL?.invoke(this)
        }
        return this
    }

    /**
     * Extensión para configurar un componente según la orientación actual
     */
    fun <T> T.ajustarSegunOrientacion(
        accionPortrait: ((T) -> Unit)? = null,
        accionLandscape: ((T) -> Unit)? = null
    ): T {
        val orientacionActual = orientacion.value ?: Orientacion.PORTRAIT
        when (orientacionActual) {
            Orientacion.PORTRAIT -> accionPortrait?.invoke(this)
            Orientacion.LANDSCAPE -> accionLandscape?.invoke(this)
        }
        return this
    }

    /**
     * Extensión para configurar un componente según el tipo de dispositivo
     */
    fun <T> T.ajustarSegunDispositivo(
        accionMovil: ((T) -> Unit)? = null,
        accionTablet: ((T) -> Unit)? = null,
        accionDesktop: ((T) -> Unit)? = null
    ): T {
        val dispositivoActual = tipoDispositivo.value ?: TipoDispositivo.MOVIL
        when (dispositivoActual) {
            TipoDispositivo.MOVIL -> accionMovil?.invoke(this)
            TipoDispositivo.TABLET -> accionTablet?.invoke(this)
            TipoDispositivo.DESKTOP -> accionDesktop?.invoke(this)
        }
        return this
    }

    private fun max(a: Int, b: Int): Int = if (a > b) a else b
}