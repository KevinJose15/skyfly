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
    destinoId INT PRIMARY KEY AUTO_INCREMENT,
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
    reservaId INT PRIMARY KEY AUTO_INCREMENT,
    clienteId INT,
    paqueteId INT,
    fechaReserva DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- <- corregido
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
CREATE TABLE Pago (
    pagoId INT PRIMARY KEY AUTO_INCREMENT,
    reservaId INT,
    monto DECIMAL(10,2),
    metodoPagoId INT,
    fechaPago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- <- corregido
    ultimos4Tarjeta VARCHAR(4),
    estadoPago VARCHAR(20) DEFAULT 'Pendiente',
    codigoAutorizacion VARCHAR(20),
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
    reservaId INT NOT NULL UNIQUE,
    fechaEmision DATE NOT NULL,
    montoTotal DECIMAL(10,2) NOT NULL,
    impuestos DECIMAL(10,2) DEFAULT 0.00,
    descuentoAplicado DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (reservaId) REFERENCES Reservas(reservaId)
) ENGINE=InnoDB;

-- Tabla: CodigoConfirmacion
CREATE TABLE CodigoConfirmacion (
    idCodigo INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(200) NOT NULL,
    codigo VARCHAR(10) NOT NULL,
    fechaGeneracion DATETIME DEFAULT CURRENT_TIMESTAMP,
    usado TINYINT DEFAULT 0
) ENGINE=InnoDB;
