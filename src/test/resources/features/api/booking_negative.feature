# language: es
@api @booking @negativo
Característica: Manejo de errores de la API de Restful Booker
  Como consumidor de la API de reservas
  Quiero recibir códigos y mensajes de error coherentes
  Para poder manejar correctamente los casos de excepción

  Antecedentes:
    Dado que Andrés tiene acceso a la API de Restful Booker
    Y se autentica con credenciales válidas

  @regresion
  Escenario: No es posible actualizar una reserva sin token de autenticación
    Cuando crea una nueva reserva
    Y intenta actualizar la reserva sin token de autenticación
    Entonces el código de respuesta debe ser 403

  @regresion
  Escenario: Consultar una reserva inexistente retorna 404
    Cuando consulta la reserva con id 99999999
    Entonces el código de respuesta debe ser 404

  @regresion
  Escenario: Eliminar una reserva sin token de autenticación no es permitido
    Cuando crea una nueva reserva
    Y intenta eliminar la reserva sin token de autenticación
    Entonces el código de respuesta debe ser 403

