# language: es
@api @booking
Característica: Ciclo de vida de una reserva en Restful Booker
  Como consumidor de la API de reservas
  Quiero crear, consultar, actualizar y eliminar reservas
  Para garantizar la integridad del recurso booking durante todo su ciclo de vida

  Antecedentes:
    Dado que usuario_API tiene acceso a la API de Restful Booker
    Y se autentica con credenciales válidas

  @regresion
  Escenario: Ciclo de vida completo (CRUD) de una reserva
    Cuando crea una nueva reserva
    Entonces el código de respuesta debe ser 200
    Y la respuesta debe contener un bookingid válido
    Y la estructura de la respuesta debe cumplir el contrato "schemas/created-booking-schema.json"
    Y los datos de la reserva deben coincidir con los datos enviados
    Y el tiempo de respuesta debe estar dentro del SLA definido
    Cuando consulta la reserva creada
    Entonces el código de respuesta debe ser 200
    Y la estructura de la respuesta debe cumplir el contrato "schemas/booking-schema.json"
    Y los datos de la reserva deben coincidir con los datos enviados
    Cuando actualiza el nombre de la reserva a "Juan"
    Entonces el código de respuesta debe ser 200
    Cuando consulta la reserva creada
    Entonces el nombre de la reserva debe ser "Juan"
    Y los demás datos de la reserva no deben haber cambiado
    Cuando elimina la reserva
    Entonces la eliminación debe ser exitosa
    Cuando consulta la reserva creada
    Entonces el código de respuesta debe ser 404
