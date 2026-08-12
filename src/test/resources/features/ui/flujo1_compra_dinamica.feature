# language: es
@ui @flujo1
Característica: Compra con selección dinámica de productos
  Como cliente de SauceDemo
  Quiero comprar el producto de menor y el de mayor precio del catálogo
  Para asegurar que la aplicación calcula correctamente los totales de mi orden

  Antecedentes:
    Dado que Diego está en la página de login de SauceDemo

  @regresion
  Escenario: Compra exitosa validando la fórmula Subtotal + Impuesto = Total
    Cuando inicia sesión con el usuario estándar
    Y ordena el catálogo por precio de menor a mayor
    Y agrega al carrito el producto de menor precio
    Y agrega al carrito el producto de mayor precio
    Y completa el proceso de checkout
    Entonces el subtotal debe corresponder a la suma de los precios de los productos agregados
    Y el total de la orden debe cumplir la fórmula Subtotal + Impuesto = Total
    Y el impuesto debe corresponder al 8% del subtotal
    Y debe visualizar la confirmación de la compra

  @regresion
  Escenario: El catálogo queda ordenado de menor a mayor precio
    Cuando inicia sesión con el usuario estándar
    Y ordena el catálogo por precio de menor a mayor
    Entonces los precios del catálogo deben estar ordenados de forma ascendente
