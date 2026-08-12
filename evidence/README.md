# Evidencias de ejecución

Esta carpeta contiene el respaldo de las ejecuciones realizadas: reportes generados,
salidas de consola y capturas de los hallazgos y del pipeline.

## Cómo consultar los reportes

**Reporte de Serenity (UI y API).** Abrir `index.html` dentro de la carpeta
`serenity-report/` correspondiente. El reporte es navegable: permite ver cada
escenario, sus pasos, tiempos de ejecución y las capturas de pantalla tomadas
ante los fallos.

**Reporte de rendimiento.** Abrir `performance/summary.html`. Los datos crudos de
todas las métricas están en `summary.json`.

## Evidencias del pipeline

Los reportes también se publican automáticamente como **artefactos de GitHub Actions**
en cada ejecución del workflow. Para consultarlos:

**Pestaña Actions → seleccionar la ejecución → sección "Artifacts" al final de la página.**

Artefactos publicados por ejecución:

| Artefacto | Contenido |
|---|---|
| `serenity-report-ui` | Reporte completo de la suite de UI |
| `serenity-report-api` | Reporte completo de la suite de API |
| `performance-report` | `summary.html` y `summary.json` de k6 |
| `serenity-report-known-issues` | Reporte de los escenarios `@bug` que documentan hallazgos |
| `qa-reports-<n>` | Paquete consolidado de todos los anteriores |

El job `publish-results` además escribe una tabla resumen visible directamente en la
página de la ejecución, sin necesidad de descargar nada.

> Los artefactos tienen una retención de 30 días. Por esa razón las evidencias
> también se adjuntan en esta carpeta del repositorio.

## Cómo ejecutar las pruebas
### Las pruebas y features estan con tags organizadas por UI, API y performance

```powershell
# Ejecutar la suite del front
mvn clean verify -Dtags="@ui"
 
# Ejecutar la suite del API
mvn clean verify -Dtags="@api"
```