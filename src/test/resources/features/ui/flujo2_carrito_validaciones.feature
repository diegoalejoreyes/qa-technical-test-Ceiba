# language: es
@ui @flujo2
Característica: Gestión del carrito y validaciones del checkout
  Como cliente de SauceDemo
  Quiero administrar los productos de mi carrito y recibir mensajes claros
  Para completar mi compra sin errores

  Antecedentes:
    Dado que Lucia está en la página de login de SauceDemo
    Y ha iniciado sesión con el usuario estándar

  @regresion
  Escenario: Eliminar el producto de mayor precio actualiza el contador del carrito
    Cuando agrega 3 productos diferentes al carrito
    Entonces el contador del carrito debe ser 3
    Cuando abre el carrito de compras
    Y elimina del carrito el producto de mayor precio
    Entonces el contador del carrito debe ser 2
    Y el producto eliminado ya no debe estar en el carrito
    Y los productos restantes deben ser los originalmente agregados

  @negativo @regresion
  Esquema del escenario: No es posible continuar el checkout sin los datos obligatorios
    Cuando agrega 3 productos diferentes al carrito
    Y abre el carrito de compras
    Y continúa al formulario de checkout
    Y intenta continuar con nombre "<nombre>", apellido "<apellido>" y código postal "<codigo>"
    Entonces debe visualizar el mensaje de error "<mensaje>"
    Y debe permanecer en el formulario de información del comprador

    Ejemplos:
      | nombre | apellido | codigo | mensaje                        |
      |        |          |        | Error: First Name is required  |
      | Lucia  |          |        | Error: Last Name is required   |
      | Lucia  | Reyes    |        | Error: Postal Code is required |
