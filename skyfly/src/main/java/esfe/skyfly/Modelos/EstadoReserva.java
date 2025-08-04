package esfe.skyfly.Modelos;

public enum EstadoReserva {
   PENDIENTE,
    CONFIRMADA,
    CANCELADA;
    
    public String getValor() {
        return this.name();
    }


    public static EstadoReserva fromString(String estado) {
        if (estado == null) return PENDIENTE;
        try {
            return EstadoReserva.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDIENTE;
        }
    }
}
