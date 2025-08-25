CREATE DATABASE AgenciaViajesDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Tabla: Usuario
CREATE TABLE usuario (
    id INT PRIMARY KEY AUTO_INCREMENT,
    "name" VARCHAR(100) NOT NULL,
    passwordHash VARCHAR(64) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    "status" bit NOT NULL, -- 1 = Activo, 0 = Inactivo
    rol ENUM('Administrador', 'Agente', 'Cliente') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE Cliente (
    clienteId INT PRIMARY KEY AUTO_INCREMENT,
    usuarioId INT UNIQUE,
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    FOREIGN KEY (usuarioId) REFERENCES usuario(id) -- ✅ La relación correcta
) ENGINE=InnoDB;

-- Tabla: Destinos
CREATE TABLE Destino (
    destino_id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100),
    pais VARCHAR(100),
    descripcion TEXT,
    imagen LONGBLOB
) ENGINE=InnoDB;

-- Tabla: Paquetes
CREATE TABLE Paquete (
    paqueteId INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(150),
    descripcion TEXT,
    precio DECIMAL(10,2),
    duracionDias INT,
    fechaInicio DATE,
    fechaFin DATE,
    destinoId INT,
    FOREIGN KEY (destinoId) REFERENCES Destino(destinoId)
) ENGINE=InnoDB;

-- Tabla: Reservas
CREATE TABLE Reservas (
    reserva_id INT PRIMARY KEY AUTO_INCREMENT,
    cliente_id INT,
    paquete_id INT,
    fecha_reserva DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- <- corregido
    estado ENUM('Pendiente', 'Confirmada', 'Cancelada') DEFAULT 'Pendiente',
    FOREIGN KEY (clienteId) REFERENCES Cliente(clienteId),
    FOREIGN KEY (paqueteId) REFERENCES Paquete(paqueteId)
) ENGINE=InnoDB;

-- Tabla: MetodoPago
CREATE TABLE MetodoPago (
    metodoPagoId INT PRIMARY KEY AUTO_INCREMENT,
    nombreMetodo VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB;

-- Tabla: Pagos
CREATE TABLE pago (
    pago_id INT PRIMARY KEY AUTO_INCREMENT,
    reserva_id INT,
    monto DECIMAL(10,2),
    metodo_pago_id INT,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- <- corregido
    ultimos4Tarjeta VARCHAR(4),
    estado_pago VARCHAR(20) DEFAULT 'Pendiente',
    codigo_autorizacion VARCHAR(20),
    FOREIGN KEY (reservaId) REFERENCES Reservas(reservaId),
    FOREIGN KEY (metodoPagoId) REFERENCES MetodoPago(metodoPagoId)
) ENGINE=InnoDB;

-- Insertar métodos de pago base
INSERT INTO MetodoPago (nombreMetodo) VALUES
('Efectivo'),
('Tarjeta'),
('Transferencia');

-- Tabla: Factura
CREATE TABLE Factura (
    idFactura INT PRIMARY KEY AUTO_INCREMENT,
    reserva_id INT NOT NULL UNIQUE,
    fecha_emision DATE NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    impuestos DECIMAL(10,2) DEFAULT 0.00,
    descuento_aplicado DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (reservaId) REFERENCES Reservas(reservaId)
) ENGINE=InnoDB;

-- Tabla: CodigoConfirmacion
CREATE TABLE CodigoConfirmacion (
    id_codigo INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(200) NOT NULL,
    codigo VARCHAR(10) NOT NULL,
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    usado TINYINT DEFAULT 0
) ENGINE=InnoDB;
