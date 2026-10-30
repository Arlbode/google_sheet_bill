# Guía didáctica de Hogar finanzas 1.6.1

Añade ocho temas, 34 pasos y ocho demostraciones ficticias sobre respaldo cifrado, estados de sincronización, historial de precios, comparación de unidades, catálogo/fotos, lista compartida, previsión y ofertas. La biblioteca completa conserva los temas anteriores: 21 temas y 102 pasos, con búsqueda, progreso y reproducción automática. Incluye accesos a los ocho temas y un botón «Aprender estas mejoras paso a paso» desde Perfil → Mejoras del hogar.

Los textos distinguen el respaldo completo .hgf de la exportación JSON antigua, explican recuperación sobre instalación vacía, contraseñas, migración de conexión, el despliegue del script 1.6.0 sin reinicializar una hoja funcional, conflictos, tasas guardadas y los límites de las estimaciones. Las ofertas se describen como referencias manuales, sin API ni scraping de Revolico.

Los ejemplos funcionan dentro de la guía: no reciben estado financiero, credenciales ni el puente nativo. En la app solo se guardan las preferencias de progreso por el mecanismo existente. Se conservan los identificadores de los temas anteriores.

## Fuentes y construcción

`new-guide-topics.js` contiene los ocho capítulos incorporados a `../1.6.0/assets/guide.js`. Las modificaciones de estilo están en `guide.css`; el acceso desde Mejoras del hogar, en `app.js` y su fuente `ui-extension.js`.

Desde el checkout existente, sin crear worktrees:

```sh
python3 development/1.6.1/build.py
.venv/bin/python development/1.6.1/tests/browser_guide.py
.venv/bin/python development/1.6.0/tests/browser_features.py
```

El constructor parte de la APK 1.6.0 verificada (la copia `Hogar_finanzas_1.6.0_CANDIDATA.apk` tiene los mismos bytes que la publicación estable). Cambia versión a 1.6.1/código 19 y solo tres assets: guía JS, guía CSS y app JS para el acceso didáctico. El DEX, los recursos compilados y los demás archivos originales permanecen idénticos. No necesita recompilar clases ni acceder a la hoja privada. Comprueba alineación, paquete, versión y certificado real después de firmar con la clave existente fuera del repositorio.

Las pruebas Chromium recorren los ocho capítulos completos, prueban demostraciones y reproducción automática, búsqueda, progreso anterior y ancho móvil. Un puente trampa exige cero llamadas nativas desde la guía aislada. La prueba integrada permite solo guideRead/guideWrite y comprueba que no cambian operaciones, cesta ni datos de las mejoras.

Si el script 1.6.0 ya está desplegado, esta actualización de la guía no requiere cambiar Apps Script. Instalar encima de la versión actual sin desinstalar.
