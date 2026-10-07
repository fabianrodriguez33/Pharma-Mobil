package pe.edu.upeu.pharmamobil.platform

/**
 * Formatea un valor numérico al formato de moneda del Perú (Ej. "S/ 10.00").
 * Declaración expect pura sin dependencias ni estado.
 */
expect fun formatearSoles(valor: Double): String
