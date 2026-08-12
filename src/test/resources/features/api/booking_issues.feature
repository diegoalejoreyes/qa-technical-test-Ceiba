# language: es

    # Escenario que documenta el hallazgo API-002 (la API responde 500 en lugar de 400).
  # Se etiqueta con @hallazgo para excluirlo de la ejecución bloqueante del pipeline

@hallazgo
Característica: Escenarios que documentan hallazgos abiertos

  Escenario: Crear una reserva sin campos obligatorios debe retornar un error controlado
    Dado que Andrés tiene acceso a la API de Restful Booker
    Y se autentica con credenciales válidas
    Cuando intenta crear una reserva sin los campos obligatorios
    Entonces el código de respuesta debe ser 400