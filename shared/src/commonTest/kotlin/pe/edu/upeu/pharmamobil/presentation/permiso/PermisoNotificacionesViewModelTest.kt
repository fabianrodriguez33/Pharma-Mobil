package pe.edu.upeu.pharmamobil.presentation.permiso

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobil.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobil.domain.permiso.Permiso
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Implementacion falsa: la ventaja de haber declarado el gestor como interfaz. */
private class GestorFalso(
    var actual: EstadoPermiso,
    var alSolicitar: EstadoPermiso = EstadoPermiso.Concedido
) : GestorPermisos {
    var solicitudes = 0
    var ajustesAbiertos = 0

    override suspend fun estado(permiso: Permiso) = actual

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        solicitudes++
        actual = alSolicitar
        return alSolicitar
    }

    override fun abrirAjustes() {
        ajustesAbiertos++
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PermisoNotificacionesViewModelTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun concedidoContinuaSinMostrarNingunDialogo() {
        val gestor = GestorFalso(EstadoPermiso.Concedido)
        val vm = PermisoNotificacionesViewModel(gestor)

        vm.alPulsarActivar()

        assertEquals(EstadoPermiso.Concedido, vm.estado.value.estado)
        assertEquals(DialogoPermiso.NINGUNO, vm.estado.value.dialogo)
        assertEquals(0, gestor.solicitudes)
    }

    @Test
    fun denegadoExplicaAntesDePedirYAlAceptarLoConcede() {
        val gestor = GestorFalso(EstadoPermiso.Denegado, alSolicitar = EstadoPermiso.Concedido)
        val vm = PermisoNotificacionesViewModel(gestor)

        vm.alPulsarActivar()
        assertEquals(DialogoPermiso.EXPLICAR, vm.estado.value.dialogo)
        assertEquals(0, gestor.solicitudes)

        vm.alConfirmarExplicacion()

        assertEquals(1, gestor.solicitudes)
        assertEquals(EstadoPermiso.Concedido, vm.estado.value.estado)
        assertEquals(DialogoPermiso.NINGUNO, vm.estado.value.dialogo)
    }

    @Test
    fun siElUsuarioDiceAhoraNoNuncaSePideElPermiso() {
        val gestor = GestorFalso(EstadoPermiso.Denegado)
        val vm = PermisoNotificacionesViewModel(gestor)

        vm.alPulsarActivar()
        vm.alCerrarDialogo()

        assertEquals(0, gestor.solicitudes)
        assertEquals(DialogoPermiso.NINGUNO, vm.estado.value.dialogo)
    }

    @Test
    fun siDenegaPorSegundaVezSeOfreceIrAAjustes() {
        val gestor = GestorFalso(EstadoPermiso.Denegado, alSolicitar = EstadoPermiso.DenegadoParaSiempre)
        val vm = PermisoNotificacionesViewModel(gestor)

        vm.alPulsarActivar()
        vm.alConfirmarExplicacion()

        assertEquals(EstadoPermiso.DenegadoParaSiempre, vm.estado.value.estado)
        assertEquals(DialogoPermiso.IR_A_AJUSTES, vm.estado.value.dialogo)

        vm.alIrAAjustes()

        assertEquals(1, gestor.ajustesAbiertos)
        assertEquals(DialogoPermiso.NINGUNO, vm.estado.value.dialogo)
    }

    @Test
    fun denegadoParaSiempreLlevaDirectoAAjustesSinVolverAPedir() {
        val gestor = GestorFalso(EstadoPermiso.DenegadoParaSiempre)
        val vm = PermisoNotificacionesViewModel(gestor)

        vm.alPulsarActivar()

        assertEquals(DialogoPermiso.IR_A_AJUSTES, vm.estado.value.dialogo)
        assertEquals(0, gestor.solicitudes)
    }

    @Test
    fun alVolverDeAjustesElEstadoSeRefresca() {
        val gestor = GestorFalso(EstadoPermiso.DenegadoParaSiempre)
        val vm = PermisoNotificacionesViewModel(gestor)

        gestor.actual = EstadoPermiso.Concedido
        vm.refrescar()

        assertEquals(EstadoPermiso.Concedido, vm.estado.value.estado)
    }
}
