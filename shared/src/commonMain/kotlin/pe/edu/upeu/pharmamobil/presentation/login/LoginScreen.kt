package pe.edu.upeu.pharmamobil.presentation.login

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import pe.edu.upeu.pharmamobil.theme.TealMarca
import pharmamobil.shared.generated.resources.Res
import pharmamobil.shared.generated.resources.logo_principal

private val TealClaro = Color(0xFF45CDB4)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    aviso: String? = null,
    oscuro: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    onAlternarTema: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current
    var claveVisible by rememberSaveable { mutableStateOf(false) }

    FondoLiquido(oscuro = oscuro, modifier = modifier.fillMaxSize()) {
        if (onAlternarTema != null) {
            BotonTema(
                oscuro = oscuro,
                onClick = onAlternarTema,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(12.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MarcaLogin(oscuro = oscuro)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp)
                    .sombraDeVidrio(oscuro)
            ) {
                PanelVidrio(oscuro = oscuro, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Bienvenido",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ingresa con tu cuenta para continuar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        aviso?.let {
                            Mensaje(texto = it, esError = false)
                        }

                        CampoVidrio(
                            value = ui.usuario,
                            onValueChange = viewModel::onUsuarioChange,
                            etiqueta = "Usuario",
                            icono = Icons.Default.Person,
                            oscuro = oscuro,
                            enabled = !ui.cargando,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { foco.moveFocus(FocusDirection.Down) }
                            )
                        )

                        CampoVidrio(
                            value = ui.clave,
                            onValueChange = viewModel::onClaveChange,
                            etiqueta = "Contraseña",
                            icono = Icons.Default.Lock,
                            oscuro = oscuro,
                            enabled = !ui.cargando,
                            visualTransformation = if (claveVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    foco.clearFocus()
                                    viewModel.ingresar()
                                }
                            ),
                            trailing = {
                                IconButton(onClick = { claveVisible = !claveVisible }) {
                                    Icon(
                                        imageVector = if (claveVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )

                        (ui.estado as? EstadoLogin.Error)?.let {
                            Mensaje(texto = it.mensaje, esError = true)
                        }

                        BotonIngresar(
                            cargando = ui.cargando,
                            onClick = {
                                foco.clearFocus()
                                viewModel.ingresar()
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Marca: medallon de vidrio con el logo y el nombre compuesto en dos pesos. */
@Composable
private fun MarcaLogin(oscuro: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(TealMarca.copy(alpha = if (oscuro) 0.45f else 0.38f), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.95f
                        ),
                        radius = size.minDimension * 0.95f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            PanelVidrio(
                oscuro = oscuro,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.size(84.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(Res.drawable.logo_principal),
                        contentDescription = "PharmaMobil",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Light)) { append("Pharma") }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (oscuro) TealClaro else Color(0xFF1E8C79))) {
                    append("Mobil")
                }
            },
            fontSize = 30.sp,
            letterSpacing = (-0.6).sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            text = "GESTIÓN FARMACÉUTICA",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Fondo: base tintada con luces de color a la deriva; es lo que el vidrio "refracta". */
@Composable
private fun FondoLiquido(
    oscuro: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transicion = rememberInfiniteTransition(label = "fondo-login")
    val deriva by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "deriva"
    )

    val baseArriba = if (oscuro) Color(0xFF07130F) else Color(0xFFF3FBF9)
    val baseAbajo = if (oscuro) Color(0xFF0B1F1B) else Color(0xFFD9F0EB)
    val luces = if (oscuro) {
        listOf(TealMarca.copy(alpha = 0.50f), Color(0xFF2B8FA3).copy(alpha = 0.34f), Color(0xFF1E8C79).copy(alpha = 0.55f))
    } else {
        listOf(TealMarca.copy(alpha = 0.55f), Color(0xFF5ED3E6).copy(alpha = 0.42f), Color(0xFFA9EBC0).copy(alpha = 0.65f))
    }

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(baseArriba, baseAbajo)))
            .drawBehind {
                val w = size.width
                val h = size.height
                fun luz(color: Color, cx: Float, cy: Float, r: Float) = drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r
                    )
                )
                luz(luces[0], w * (0.10f + 0.22f * deriva), h * (0.10f + 0.06f * deriva), w * 0.95f)
                luz(luces[1], w * (0.95f - 0.20f * deriva), h * (0.42f - 0.08f * deriva), w * 0.80f)
                luz(luces[2], w * (0.15f + 0.18f * deriva), h * (0.92f - 0.10f * deriva), w * 1.05f)
            }
    ) {
        content()
    }
}

/** Resplandor suave bajo el panel, para que el vidrio parezca flotar. */
private fun Modifier.sombraDeVidrio(oscuro: Boolean): Modifier = drawBehind {
    val alto = size.height
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (oscuro) Color.Black else Color(0xFF1E8C79)).copy(alpha = if (oscuro) 0.50f else 0.22f),
                Color.Transparent
            ),
            center = Offset(size.width / 2f, alto * 0.98f),
            radius = size.width * 0.62f
        ),
        topLeft = Offset(0f, alto * 0.55f),
        size = androidx.compose.ui.geometry.Size(size.width, alto * 0.9f)
    )
}

/** Superficie de vidrio: relleno translucido, brillo especular arriba-izquierda y borde de luz. */
@Composable
private fun PanelVidrio(
    oscuro: Boolean,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(32.dp),
    content: @Composable () -> Unit
) {
    val relleno = if (oscuro) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.05f)))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.78f), Color.White.copy(alpha = 0.46f)))
    }
    val borde = if (oscuro) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.06f), TealMarca.copy(alpha = 0.35f)))
    } else {
        Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.25f), TealMarca.copy(alpha = 0.45f)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(relleno)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = if (oscuro) 0.16f else 0.55f), Color.Transparent),
                        center = Offset(size.width * 0.12f, 0f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .border(1.2.dp, borde, shape)
    ) {
        content()
    }
}

@Composable
private fun CampoVidrio(
    value: String,
    onValueChange: (String) -> Unit,
    etiqueta: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    oscuro: Boolean,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null
) {
    val fondo = if (oscuro) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.62f)
    val fondoFoco = if (oscuro) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.92f)
    val reposo = if (oscuro) Color.White.copy(alpha = 0.18f) else Color.White
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = trailing,
        singleLine = true,
        enabled = enabled,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fondoFoco,
            unfocusedContainerColor = fondo,
            disabledContainerColor = fondo,
            focusedBorderColor = TealMarca,
            unfocusedBorderColor = reposo,
            disabledBorderColor = reposo,
            focusedLeadingIconColor = TealMarca,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = if (oscuro) TealClaro else Color(0xFF1E8C79),
            cursorColor = TealMarca
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun Mensaje(texto: String, esError: Boolean) {
    val esquema = MaterialTheme.colorScheme
    val fondo = if (esError) esquema.errorContainer else esquema.tertiaryContainer
    val contenido = if (esError) esquema.onErrorContainer else esquema.onTertiaryContainer
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = contenido, modifier = Modifier.size(18.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = contenido)
    }
}

@Composable
private fun BotonIngresar(
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(18.dp)
    val relleno = remember { Brush.horizontalGradient(listOf(TealMarca, TealClaro)) }
    Surface(
        onClick = onClick,
        enabled = !cargando,
        shape = forma,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(relleno)
                .drawBehind {
                    // Reflejo de luz en el borde superior, como un bisel de cristal.
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.38f), Color.Transparent),
                            endY = size.height * 0.55f
                        )
                    )
                }
                .border(1.dp, Color.White.copy(alpha = 0.55f), forma),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (cargando) "Ingresando…" else "Ingresar",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Interruptor claro/oscuro en vidrio, arriba a la derecha. */
@Composable
private fun BotonTema(oscuro: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PanelVidrio(oscuro = oscuro, shape = RoundedCornerShape(16.dp), modifier = modifier.size(46.dp)) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = if (oscuro) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (oscuro) "Cambiar a modo claro" else "Cambiar a modo oscuro",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
